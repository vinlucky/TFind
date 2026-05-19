package com.tfind.user.service;

import com.tfind.user.entity.User;
import java.util.List;

public interface UserService {

    User register(String userId, String password);

    String login(String userId, String password);

    User getUserByUserId(String userId);

    void updatePassword(String userId, String oldPwd, String newPwd);

    void updateUserId(String oldUserId, String newUserId);

    void softDeleteUser(String userId);

    void restoreUser(String userId);

    int physicalDeleteUsers();

    void deleteUserByAdmin(String userId);

    void addAdmin(String userId);

    List<User> getAllDeletedUsers();

    List<User> getAllUsers();

    void physicalDeleteSingleUser(String userId);

    void changeRole(String userId, String role);
}
