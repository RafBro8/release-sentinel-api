package com.releasesentinel.api.dto;

import com.releasesentinel.domain.Project;
import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String key,
        String name,
        String description,
        Instant createdAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt());
    }
}
