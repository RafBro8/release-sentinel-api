package com.releasesentinel.api.dto;

import com.releasesentinel.domain.DefectStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDefectStatusRequest(@NotNull DefectStatus status) {
}
