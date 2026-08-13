package com.releasesentinel.api.dto;

import com.releasesentinel.domain.EnvironmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEnvironmentRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull EnvironmentType type,
        @Size(max = 255) String baseUrl) {
}
