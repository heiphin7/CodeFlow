package com.api.codeflow.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TaskSubmissionDto {
    private String status;
    private String language;
    private Double memoryUsage; // mbs
    private Double timeUsage; // seconds
    private String code;
    private Date submittedTime;
}
