package com.api.codeflow.dto.response;

import lombok.Data;

@Data
public class WrongSolution {
    private Integer testCaseNumber;
    private String input;
    private String exceptedOutput;
    private String programOutput;
}
