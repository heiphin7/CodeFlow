package com.api.codeflow.dto.judge0;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchSubmissionTokenResponse {
    private List<BatchSubmissionToken> tokens;
}
