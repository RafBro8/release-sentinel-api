package com.releasesentinel.api.dto;

import com.releasesentinel.domain.QualityStatus;
import java.util.List;
import java.util.UUID;

public record ReleaseQualitySummaryResponse(
        UUID releaseId,
        UUID projectId,
        String releaseVersion,
        QualityStatus status,
        int totalTests,
        int passed,
        int failed,
        int skipped,
        int blocked,
        double passRate,
        int openDefects,
        int openCriticalDefects,
        int openHighDefects,
        int blockingDefects,
        String recommendation,
        List<String> riskReasons) {
}
