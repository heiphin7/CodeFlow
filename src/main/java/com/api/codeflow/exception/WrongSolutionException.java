package com.api.codeflow.exception;

import com.api.codeflow.dto.response.WrongSolution;

public class WrongSolutionException extends RuntimeException {
    private final WrongSolution payload;
    public WrongSolutionException(WrongSolution payload) {
        super("Test case " + payload.getTestCaseNumber() + " failed");
        this.payload = payload;
    }
    public WrongSolution getPayload() {
        return payload;
    }
}

