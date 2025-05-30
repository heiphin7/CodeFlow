package com.api.codeflow.exception;

import com.api.codeflow.dto.response.WrongSolution;

public class OutOfMemoryException extends RuntimeException {
    private final WrongSolution payload;

    public OutOfMemoryException(WrongSolution payload) {
        super("Out of memory on test case " + payload.getTestCaseNumber());
        this.payload = payload;
    }

    public WrongSolution getPayload() {
        return payload;
    }
}
