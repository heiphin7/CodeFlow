package com.api.codeflow.dto.response;

import lombok.Data;

@Data
public class SuccessSolution {
    private Double memoryUsage;
    private Integer timeUsage; // in millis
}
