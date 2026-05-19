package com.tfind.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tfind.common.constant.CommonConstant;
import com.tfind.common.exception.BusinessException;
import com.tfind.common.util.JwtUtil;
import com.tfind.common.util.SM3Util;
import com.tfind.user.entity.User;
import com.tfind.user.mapper.UserMapper;
import com.tfind.user.mq.UserMqProducer;
import com.tfind.user.service.UserService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMqProducer userMqProducer;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Override
    public User register(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BusinessException("userId不能为空");
        }
        if (!userId.matches("^[a-zA-Z0-9_\\-\\.\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=]+$")) {
            throw new BusinessException("userId只允许英文字母数字和普通符号");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户ID已存在");
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(SM3Util.hash(password));
        user.setOpenid(UUID.randomUUID().toString());
        user.setRole("USER");
        user.setDelUser(false);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        userMqProducer.sendUserRegisterMessage(userId, user.getOpenid());

        user.setPassword(null);
        return user;
    }

    @Override
    public String login(String userId, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Boolean.TRUE.equals(user.getDelUser())) {
            throw new BusinessException("用户已被删除");
        }
        if (!SM3Util.hash(password).equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String openid = user.getOpenid();
        if (openid == null || openid.isEmpty()) {
            userMqProducer.sendUserRegisterMessage(userId, "");
            openid = user.getOpenid();
        }

        String token = JwtUtil.generateToken(userId, user.getRole(), jwtSecretKey, jwtExpirationMs);

        user.setPassword(null);
        user.setOpenid(openid);
        redisTemplate.opsForValue().set(
                CommonConstant.REDIS_USER_PREFIX + userId,
                user,
                24,
                TimeUnit.HOURS
        );

        return token;
    }

    @Override
    public User getUserByUserId(String userId) {
        Object cached = redisTemplate.opsForValue().get(CommonConstant.REDIS_USER_PREFIX + userId);
        if (cached instanceof User) {
            return (User) cached;
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            user.setPassword(null);
            redisTemplate.opsForValue().set(
                    CommonConstant.REDIS_USER_PREFIX + userId,
                    user,
                    24,
                    TimeUnit.HOURS
            );
        }

        return user;
    }

    @Override
    public void updatePassword(String userId, String oldPwd, String newPwd) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!SM3Util.hash(oldPwd).equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getPassword, SM3Util.hash(newPwd))
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);
    }

    @Override
    public void updateUserId(String oldUserId, String newUserId) {
        if (newUserId == null || newUserId.trim().isEmpty()) {
            throw new BusinessException("新userId不能为空");
        }
        if (!newUserId.matches("^[a-zA-Z0-9_\\-\\.\\@\\#\\$\\%\\^\\&\\*\\(\\)\\+\\=]+$")) {
            throw new BusinessException("userId只允许英文字母数字和普通符号");
        }

        LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(User::getUserId, newUserId);
        if (userMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("新用户ID已存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, oldUserId)
                .set(User::getUserId, newUserId)
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + oldUserId);

        userMqProducer.sendUserIdChangeMessage(oldUserId, newUserId);
    }

    @Override
    public void softDeleteUser(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getDelUser, true)
                .set(User::getDelTime, LocalDateTime.now())
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);

        userMqProducer.sendUserDeleteMessage(userId);
    }

    @Override
    public void restoreUser(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getDelUser, false)
                .set(User::getDelTime, null)
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);

        userMqProducer.sendUserRestoreMessage(userId);
    }

    @Override
    public int physicalDeleteUsers() {
        LocalDateTime threshold = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDelUser, true)
                .lt(User::getDelTime, threshold);
        List<User> users = userMapper.selectList(queryWrapper);

        int count = 0;
        for (User user : users) {
            userMapper.deleteById(user.getId());
            redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + user.getUserId());
            count++;
        }
        return count;
    }

    @Override
    public void deleteUserByAdmin(String userId) {
        User user = getUserByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员用户");
        }
        softDeleteUser(userId);
    }

    @Override
    public void addAdmin(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getRole, "ADMIN")
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);

        userMqProducer.sendUserRoleChangeMessage(userId, "ADMIN");
    }

    @Override
    public List<User> getAllDeletedUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDelUser, true);
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public List<User> getAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDelUser, false);
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public void physicalDeleteSingleUser(String userId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            userMapper.deleteById(user.getId());
            redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);
        }
    }

    @Override
    public void changeRole(String userId, String role) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getRole, role)
                .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);

        redisTemplate.delete(CommonConstant.REDIS_USER_PREFIX + userId);

        userMqProducer.sendUserRoleChangeMessage(userId, role);
    }
}
