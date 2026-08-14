package com.releasesentinel.api.dto;

import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestResult;
import java.time.Instant;
import java.util.UUID;

public record TestExecutionResponse(
        UUID id,
        UUID testRunId,
        UUID testCaseId,
        TestResult result,
        String notes,
        Instant executedAt) {

    public static TestExecutionResponse from(TestExecution execution) {
        return new TestExecutionResponse(
                execution.getId(),
                execution.getTestRun().getId(),
                execution.getTestCase().getId(),
                execution.getResult(),
                execution.getNotes(),
                execution.getExecutedAt());
    }
}
