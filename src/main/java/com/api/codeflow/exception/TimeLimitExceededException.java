package com.api.codeflow.exception;

public class TimeLimitExceededException extends RuntimeException {
    private final int testCaseNumber;

    public TimeLimitExceededException(int testCaseNumber) {
        super("Time limit exceeded at test case " + testCaseNumber);
        this.testCaseNumber = testCaseNumber;
    }

    public int getTestCaseNumber() {
        return testCaseNumber;
    }
}
