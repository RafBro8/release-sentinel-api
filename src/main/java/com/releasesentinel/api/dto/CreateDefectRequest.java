package com.releasesentinel.api.dto;

import com.releasesentinel.domain.DefectPriority;
import com.releasesentinel.domain.DefectSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateDefectRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 1200) String description,
        @NotNull DefectSeverity severity,
        @NotNull DefectPriority priority,
        Boolean blockingRelease,
        UUID linkedTestExecutionId) {
}
