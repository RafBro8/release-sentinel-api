package com.releasesentinel.api;

import com.releasesentinel.api.dto.CreateProjectRequest;
import com.releasesentinel.api.dto.ProjectResponse;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ReleaseTrackingService releaseTrackingService;

    public ProjectController(ReleaseTrackingService releaseTrackingService) {
        this.releaseTrackingService = releaseTrackingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request) {
        return releaseTrackingService.createProject(request);
    }

    @GetMapping
    public List<ProjectResponse> getProjects() {
        return releaseTrackingService.getProjects();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable UUID projectId) {
        return releaseTrackingService.getProject(projectId);
    }
}
