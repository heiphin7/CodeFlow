package com.api.codeflow.exception;

import com.api.codeflow.dto.judge0.TimeLimitSolution;

public class TimeLimitException extends RuntimeException {
    private final TimeLimitSolution payload;

    public TimeLimitException(TimeLimitSolution payload) {
        super("Time limit exceeded on test case " + payload.getTestCaseNumber());
        this.payload = payload;
    }

    public TimeLimitSolution getPayload() {
        return payload;
    }
}
