package com.tfind.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToiletInfo {

    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String floor;
    private Integer positions;
    private Double cleanScore;
    private Double score;
    private Double aiScore;
    private String tags;
    private String aiTags;
    private Boolean hasMotherRoom;
    private Boolean hasAccessibility;
    private Boolean isFree;
    private Boolean is24Hours;
    private Boolean isQueuing;
    private Integer queueTime;
    private String photoUrl;
    private String address;
    private String openid;
    private Integer status;
    private Integer deltoilet;
    private LocalDateTime deltime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
