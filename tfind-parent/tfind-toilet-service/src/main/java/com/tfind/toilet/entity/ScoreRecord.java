package com.tfind.toilet.entity;

import lombok.Data;

@Data
public class ScoreRecord {

    private String id;
    private String openid;
    private Integer score;
    private String action;
    private Integer balance;
    private Long createTime;
}
