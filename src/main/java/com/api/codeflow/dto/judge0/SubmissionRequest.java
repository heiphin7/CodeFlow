package com.api.codeflow.dto.judge0;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionRequest {
    private String source_code;
    private Integer language_id;
    private String stdin;
    private String expected_output;
    private Double cpu_time_limit; // в секундах
    private Integer memory_limit;  // в мегабайтах
    private Double wall_time_limit;
    private String source_file_name;
}
