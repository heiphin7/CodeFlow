package com.api.codeflow.dto.judge0;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionResponse {
    private String token;
    private Integer time;    // в миллисекундах
    private Double memory;   // в МБ
    private Status status;
    private String stdout;
    private String stderr;
}
