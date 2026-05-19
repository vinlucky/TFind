package com.tfind.toilet.entity;

import lombok.Data;

@Data
public class UserProfile {

    private String id;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private Integer score;
    private Integer uploadCount;
    private Integer approvedCount;
    private Long createTime;
    private Long lastLoginTime;
}
