package com.api.codeflow.dto;

import lombok.Data;

import java.util.Date;

@Data
public class LastSubmissionsDto { // для страницы /submissions
    private Long id;
    private String taskTitle;
    private String username;
    private Double memoryUsage; // MB
    private Double timeUsage; // seconds
    private String status;
    private Integer testCaseNumber; // must be null
    private String code;
    private String language;
    private Date uploadTime;
}
