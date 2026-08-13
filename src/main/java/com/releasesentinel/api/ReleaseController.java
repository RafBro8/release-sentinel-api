package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateReleaseRequest;
import com.releasesentinel.api.dto.ReleaseResponse;
import com.releasesentinel.service.ReleaseTrackingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReleaseController {

    private final ReleaseTrackingService releaseTrackingService;

    public ReleaseController(ReleaseTrackingService releaseTrackingService) {
        this.releaseTrackingService = releaseTrackingService;
    }

    @PostMapping("/api/projects/{projectId}/releases")
    @ResponseStatus(HttpStatus.CREATED)
    public ReleaseResponse createRelease(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateReleaseRequest request) {
        return releaseTrackingService.createRelease(projectId, request);
    }

    @GetMapping("/api/projects/{projectId}/releases")
    public List<ReleaseResponse> getProjectReleases(@PathVariable UUID projectId) {
        return releaseTrackingService.getProjectReleases(projectId);
    }

    @GetMapping("/api/releases/{releaseId}")
    public ReleaseResponse getRelease(@PathVariable UUID releaseId) {
        return releaseTrackingService.getRelease(releaseId);
    }
}
