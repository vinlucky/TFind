package com.tfind.toilet.entity;

import lombok.Data;

import java.util.List;

@Data
public class Review {

    private String id;
    private String toiletId;
    private String openid;
    private String userName;
    private String content;
    private Double score;
    private List<String> tags;
    private String status;
    private Long createTime;
}
