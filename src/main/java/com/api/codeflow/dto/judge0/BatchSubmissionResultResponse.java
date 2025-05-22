package com.api.codeflow.dto.judge0;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BatchSubmissionResultResponse {
    private List<BatchSubmissionResult> submissions;
}
