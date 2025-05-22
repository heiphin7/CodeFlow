package com.api.codeflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskInfoDto {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private Integer number;
    private String solution;
    private List<String> tags;
    private Double timeLimit;
    private Integer memoryLimit;
}
