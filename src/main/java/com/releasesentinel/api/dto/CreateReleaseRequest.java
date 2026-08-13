package com.releasesentinel.api.dto;

import com.releasesentinel.domain.ReleaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateReleaseRequest(
        @NotBlank @Size(max = 40) String version,
        ReleaseStatus status,
        LocalDate targetDate) {
}
