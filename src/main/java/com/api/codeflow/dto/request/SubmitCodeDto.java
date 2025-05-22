package com.api.codeflow.dto.request;

import lombok.Data;

@Data
public class SubmitCodeDto {
    private String solution;
    private String language;
    private Long userId;
}
