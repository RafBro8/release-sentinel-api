package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateDefectRequest;
import com.releasesentinel.api.dto.DefectResponse;
import com.releasesentinel.api.dto.UpdateDefectStatusRequest;
import com.releasesentinel.service.DefectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefectController {

    private final DefectService defectService;

    public DefectController(DefectService defectService) {
        this.defectService = defectService;
    }

    @PostMapping("/api/releases/{releaseId}/defects")
    @ResponseStatus(HttpStatus.CREATED)
    public DefectResponse createDefect(
            @PathVariable UUID releaseId,
            @Valid @RequestBody CreateDefectRequest request) {
        return defectService.createDefect(releaseId, request);
    }

    @GetMapping("/api/releases/{releaseId}/defects")
    public List<DefectResponse> getReleaseDefects(@PathVariable UUID releaseId) {
        return defectService.getReleaseDefects(releaseId);
    }

    @GetMapping("/api/projects/{projectId}/defects")
    public List<DefectResponse> getProjectDefects(@PathVariable UUID projectId) {
        return defectService.getProjectDefects(projectId);
    }

    @GetMapping("/api/defects/{defectId}")
    public DefectResponse getDefect(@PathVariable UUID defectId) {
        return defectService.getDefect(defectId);
    }

    @PatchMapping("/api/defects/{defectId}/status")
    public DefectResponse updateDefectStatus(
            @PathVariable UUID defectId,
            @Valid @RequestBody UpdateDefectStatusRequest request) {
        return defectService.updateDefectStatus(defectId, request);
    }
}
