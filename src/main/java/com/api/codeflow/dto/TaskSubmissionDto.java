package com.api.codeflow.dto;

import lombok.Data;

@Data
public class TaskSubmissionDto {
    private String status;
    private String language;
    private Double memoryUsage; // TODO: Check in KB or MB
    private Double timeUsage; // TODO: Check is millis or seconds
    private String code;
}
