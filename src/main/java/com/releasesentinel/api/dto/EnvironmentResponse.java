package com.releasesentinel.api.dto;

import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.EnvironmentType;
import java.time.Instant;
import java.util.UUID;

public record EnvironmentResponse(
        UUID id,
        UUID projectId,
        String name,
        EnvironmentType type,
        String baseUrl,
        Instant createdAt) {

    public static EnvironmentResponse from(Environment environment) {
        return new EnvironmentResponse(
                environment.getId(),
                environment.getProject().getId(),
                environment.getName(),
                environment.getType(),
                environment.getBaseUrl(),
                environment.getCreatedAt());
    }
}
