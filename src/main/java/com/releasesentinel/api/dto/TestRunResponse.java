package com.releasesentinel.api.dto;

import com.releasesentinel.domain.TestRun;
import com.releasesentinel.domain.TestRunStatus;
import java.time.Instant;
import java.util.UUID;

public record TestRunResponse(
        UUID id,
        UUID releaseId,
        UUID environmentId,
        String name,
        TestRunStatus status,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt) {

    public static TestRunResponse from(TestRun testRun) {
        return new TestRunResponse(
                testRun.getId(),
                testRun.getRelease().getId(),
                testRun.getEnvironment().getId(),
                testRun.getName(),
                testRun.getStatus(),
                testRun.getStartedAt(),
                testRun.getCompletedAt(),
                testRun.getCreatedAt());
    }
}
