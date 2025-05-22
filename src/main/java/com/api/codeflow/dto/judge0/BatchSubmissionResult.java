package com.api.codeflow.dto.judge0;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchSubmissionResult {
    private String stdout;
    private String stderr;
    private String compile_output;
    private String message;
    private String token;
    private String time;
    private Integer memory;
    private Status status;
}