package com.releasesentinel.api.dto;

import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTestCaseRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 1000) String description,
        @NotNull TestPriority priority,
        @NotNull TestType type) {
}
