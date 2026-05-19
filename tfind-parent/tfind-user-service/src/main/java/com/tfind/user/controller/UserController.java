package com.tfind.user.controller;

import com.tfind.common.result.Result;
import com.tfind.user.entity.User;
import com.tfind.user.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String password = params.get("password");
        User user = userService.register(userId, password);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String password = params.get("password");
        String token = userService.login(userId, password);
        return Result.success(token);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        User user = userService.getUserByUserId(userId);
        return Result.success(user);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success(null);
    }

    @PutMapping("/userId")
    public Result<Void> updateUserId(@RequestBody Map<String, String> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oldUserId = (String) authentication.getPrincipal();
        String newUserId = params.get("newUserId");
        userService.updateUserId(oldUserId, newUserId);
        return Result.success(null);
    }

    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUserByAdmin(userId);
        return Result.success(null);
    }

    @PutMapping("/{userId}/restore")
    public Result<Void> restoreUser(@PathVariable String userId) {
        userService.restoreUser(userId);
        return Result.success(null);
    }

    @PostMapping("/{userId}/admin")
    public Result<Void> addAdmin(@PathVariable String userId) {
        userService.addAdmin(userId);
        return Result.success(null);
    }

    @DeleteMapping("/{userId}/admin")
    public Result<Void> removeAdmin(@PathVariable String userId) {
        userService.changeRole(userId, "USER");
        return Result.success(null);
    }

    @GetMapping("/deleted")
    public Result<List<User>> getDeletedUsers() {
        List<User> users = userService.getAllDeletedUsers();
        return Result.success(users);
    }

    @DeleteMapping("/{userId}/physical")
    public Result<Void> physicalDeleteUser(@PathVariable String userId) {
        userService.physicalDeleteSingleUser(userId);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }
}
