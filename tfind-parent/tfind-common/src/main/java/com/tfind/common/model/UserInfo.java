package com.tfind.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {

    private String userId;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String role;
    private Integer score;
    private Integer deluser;
    private LocalDateTime deltime;
}
