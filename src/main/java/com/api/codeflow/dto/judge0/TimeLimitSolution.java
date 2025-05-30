package com.api.codeflow.dto.judge0;

import lombok.Data;

@Data
public class TimeLimitSolution {
    private int testCaseNumber;
    private String input;
    private String exceptedOutput;
    private String programOutput;
}
