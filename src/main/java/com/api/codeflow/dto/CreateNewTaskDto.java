package com.api.codeflow.dto;

import com.api.codeflow.model.TestCase;
import jakarta.validation.Valid;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.List;

@Data
public class CreateNewTaskDto {

    @NotBlank(message = "Заголовок обязателен")
    private String title;

    @NotBlank(message = "Сложность обязательна")
    @Pattern(regexp = "Easy|Medium|Hard", message = "Сложность должна быть Easy, Medium или Hard")
    private String difficulty;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotNull(message = "Номер задачи обязателен")
    @Min(value = 1, message = "Номер задачи должен быть >= 1")
    private Integer number;

    @NotBlank(message = "Решение задачи обязательно")
    private String solution;

    @NotEmpty(message = "Нужен хотя бы один тест")
    private List<@Valid TestCase> testCases;

    private List<@NotBlank(message = "Тег не должен быть пустым") String> tags;

    @NotNull(message = "Лимит по времени обязателен")
    @DecimalMin(value = "0.01", message = "Лимит времени должен быть больше 0")
    private Double timeLimit;

    @NotNull(message = "Лимит по памяти обязателен")
    @Min(value = 1, message = "Лимит памяти должен быть больше 0")
    private Integer memoryLimit;
}
