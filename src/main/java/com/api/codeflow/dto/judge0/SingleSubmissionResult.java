package com.api.codeflow.dto.judge0;

import lombok.Data;

@Data
public class SingleSubmissionResult {
    private String token;
    private String stdout;
    private String stderr;
    private String compile_output;
    private String message;
    private String time;       // время в секундах, строкой
    private Integer memory;    // в байтах
    private SubmissionStatus status;
}
