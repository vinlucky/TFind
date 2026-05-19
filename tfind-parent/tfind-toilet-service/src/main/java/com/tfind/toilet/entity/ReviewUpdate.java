package com.tfind.toilet.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReviewUpdate {

    private String id;
    private String toiletId;
    private String openid;
    private String userName;
    private Double score;
    private List<String> tags;
    private Boolean isFree;
    private Boolean hasMotherRoom;
    private Boolean hasAccessibility;
    private Boolean is24Hours;
    private Boolean isQueuing;
    private Integer queueTime;
    private String content;
    private String status;
    private Long createTime;
}
