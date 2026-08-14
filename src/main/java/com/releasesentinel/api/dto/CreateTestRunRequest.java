package com.releasesentinel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateTestRunRequest(
        @NotNull UUID environmentId,
        @NotBlank @Size(max = 160) String name) {
}
