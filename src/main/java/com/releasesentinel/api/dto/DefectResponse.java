package com.releasesentinel.api.dto;

import com.releasesentinel.domain.Defect;
import com.releasesentinel.domain.DefectPriority;
import com.releasesentinel.domain.DefectSeverity;
import com.releasesentinel.domain.DefectStatus;
import java.time.Instant;
import java.util.UUID;

public record DefectResponse(
        UUID id,
        UUID projectId,
        UUID releaseId,
        UUID linkedTestExecutionId,
        String title,
        String description,
        DefectSeverity severity,
        DefectPriority priority,
        DefectStatus status,
        boolean blockingRelease,
        Instant createdAt,
        Instant updatedAt) {

    public static DefectResponse from(Defect defect) {
        UUID linkedExecutionId = defect.getLinkedTestExecution() == null
                ? null
                : defect.getLinkedTestExecution().getId();

        return new DefectResponse(
                defect.getId(),
                defect.getProject().getId(),
                defect.getRelease().getId(),
                linkedExecutionId,
                defect.getTitle(),
                defect.getDescription(),
                defect.getSeverity(),
                defect.getPriority(),
                defect.getStatus(),
                defect.isBlockingRelease(),
                defect.getCreatedAt(),
                defect.getUpdatedAt());
    }
}
