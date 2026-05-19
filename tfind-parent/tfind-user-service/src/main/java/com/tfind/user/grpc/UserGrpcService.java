package com.tfind.user.grpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tfind.user.entity.User;
import com.tfind.user.mapper.UserMapper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void getUser(UserServiceProto.UserRequest request, StreamObserver<UserServiceProto.UserResponse> responseObserver) {
        String userId = request.getUserId();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = userMapper.selectOne(queryWrapper);

        UserServiceProto.UserResponse.Builder builder = UserServiceProto.UserResponse.newBuilder();
        if (user != null) {
            builder.setUserId(user.getUserId() != null ? user.getUserId() : "")
                    .setOpenid(user.getOpenid() != null ? user.getOpenid() : "")
                    .setNickname("")
                    .setAvatarUrl("")
                    .setRole(user.getRole() != null ? user.getRole() : "")
                    .setScore(0)
                    .setDeluser(Boolean.TRUE.equals(user.getDelUser()));
        } else {
            builder.setUserId("")
                    .setOpenid("")
                    .setNickname("")
                    .setAvatarUrl("")
                    .setRole("")
                    .setScore(0)
                    .setDeluser(false);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserIdByOpenid(UserServiceProto.OpenidRequest request, StreamObserver<UserServiceProto.UserIdResponse> responseObserver) {
        String openid = request.getOpenid();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(queryWrapper);

        UserServiceProto.UserIdResponse.Builder builder = UserServiceProto.UserIdResponse.newBuilder();
        if (user != null) {
            builder.setUserId(user.getUserId() != null ? user.getUserId() : "");
        } else {
            builder.setUserId("");
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
