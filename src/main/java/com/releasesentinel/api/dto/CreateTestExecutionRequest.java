package com.releasesentinel.api.dto;

import com.releasesentinel.domain.TestResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateTestExecutionRequest(
        @NotNull UUID testCaseId,
        @NotNull TestResult result,
        @Size(max = 1000) String notes) {
}
