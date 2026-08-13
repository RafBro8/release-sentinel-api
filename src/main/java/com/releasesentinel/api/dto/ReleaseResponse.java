package com.releasesentinel.api.dto;

import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReleaseResponse(
        UUID id,
        UUID projectId,
        String version,
        ReleaseStatus status,
        LocalDate targetDate,
        Instant createdAt) {

    public static ReleaseResponse from(Release release) {
        return new ReleaseResponse(
                release.getId(),
                release.getProject().getId(),
                release.getVersion(),
                release.getStatus(),
                release.getTargetDate(),
                release.getCreatedAt());
    }
}
