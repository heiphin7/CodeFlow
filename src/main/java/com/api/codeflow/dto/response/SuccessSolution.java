package com.api.codeflow.dto.response;

import com.api.codeflow.dto.AnalysisResultDto;
import lombok.Data;

@Data
public class SuccessSolution {
    private Double memoryUsage;
    private Integer timeUsage; // in millis
    private AnalysisResultDto dto;
}
