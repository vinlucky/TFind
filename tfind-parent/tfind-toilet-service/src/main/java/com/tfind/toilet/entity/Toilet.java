package com.tfind.toilet.entity;

import lombok.Data;

import java.util.List;

@Data
public class Toilet {

    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String floor;
    private Integer positions;
    private Double cleanScore;
    private Double score;
    private Double aiScore;
    private List<String> tags;
    private List<String> aiTags;
    private Boolean hasMotherRoom;
    private Boolean hasAccessibility;
    private Boolean isFree;
    private Boolean is24Hours;
    private Boolean isQueuing;
    private Integer queueTime;
    private String photoUrl;
    private String address;
    private String openid;
    private String status;
    private Boolean deltoilet;
    private Long deltime;
    private String aiAnalysis;
    private String reviewer;
    private Long createTime;
    private Long updateTime;
}
