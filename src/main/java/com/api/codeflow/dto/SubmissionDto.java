package com.api.codeflow.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SubmissionDto {
    private Long id;
    private String language;
    private String status;
    private Double executionTime;
    private Double memoryUsage;
    private Date createdAt;
    private String taskTitle;
}
