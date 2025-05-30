package com.api.codeflow.exception;

import com.api.codeflow.dto.judge0.CompilationErrorSolution;

public class CompilationErrorException extends RuntimeException {
    private final CompilationErrorSolution payload;

    public CompilationErrorException(CompilationErrorSolution payload) {
        super("Compilation Error on test case " + payload.getTestCaseNumber());
        this.payload = payload;
    }

    public CompilationErrorSolution getPayload() {
        return payload;
    }
}
