package com.tfind.toilet.entity;

import lombok.Data;

@Data
public class Report {

    private String id;
    private String toiletId;
    private String toiletName;
    private String openid;
    private String reportType;
    private String reportTypeName;
    private String content;
    private String status;
    private Long createTime;
}
