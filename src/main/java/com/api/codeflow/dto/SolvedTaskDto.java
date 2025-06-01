package com.api.codeflow.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SolvedTaskDto {
    private Long id;
    private String taskName;
    private Double timeUsage; // in millis
    private Double memoryUsage; // in mb's
    private Date solvedAt;
    private String code;
    private String language;
}
