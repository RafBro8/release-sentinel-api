package com.releasesentinel.api.dto;

import com.releasesentinel.domain.TestCase;
import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestType;
import java.time.Instant;
import java.util.UUID;

public record TestCaseResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TestPriority priority,
        TestType type,
        boolean active,
        Instant createdAt) {

    public static TestCaseResponse from(TestCase testCase) {
        return new TestCaseResponse(
                testCase.getId(),
                testCase.getProject().getId(),
                testCase.getTitle(),
                testCase.getDescription(),
                testCase.getPriority(),
                testCase.getType(),
                testCase.isActive(),
                testCase.getCreatedAt());
    }
}
