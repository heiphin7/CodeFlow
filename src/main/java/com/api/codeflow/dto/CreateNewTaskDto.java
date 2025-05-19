package com.api.codeflow.dto;

import com.api.codeflow.model.TestCase;
import lombok.Data;

import java.util.List;

@Data
public class CreateNewTaskDto {
    private String title;
    private String difficulty; // Easy, Medium, Hard
    private String description; // Можно вот как блоги делать типа в которых прямо html
    private Integer number; // Номер задачи
    private String solution; // Код от создателя напрямую
    private List<TestCase> testCases;
    private List<String> tags;

    // Ограничения по выполнению кода
    private Double timeLimit;    // в секундах
    private Integer memoryLimit; // в мегабайтах
}
