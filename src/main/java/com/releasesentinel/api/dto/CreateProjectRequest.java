package com.releasesentinel.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "must start with a letter and contain only uppercase letters, numbers, or underscores")
        String key,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description) {
}
