package com.releasesentinel.api;

import com.releasesentinel.api.dto.ReleaseQualitySummaryResponse;
import com.releasesentinel.service.QualityGateService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QualityGateController {

    private final QualityGateService qualityGateService;

    public QualityGateController(QualityGateService qualityGateService) {
        this.qualityGateService = qualityGateService;
    }

    @GetMapping("/api/releases/{releaseId}/quality-summary")
    public ReleaseQualitySummaryResponse getReleaseQualitySummary(@PathVariable UUID releaseId) {
        return qualityGateService.getReleaseQualitySummary(releaseId);
    }
}
