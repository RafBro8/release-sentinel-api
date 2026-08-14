package com.releasesentinel.service;

import com.releasesentinel.api.dto.CreateEnvironmentRequest;
import com.releasesentinel.api.dto.CreateProjectRequest;
import com.releasesentinel.api.dto.CreateReleaseRequest;
import com.releasesentinel.api.dto.EnvironmentResponse;
import com.releasesentinel.api.dto.ProjectResponse;
import com.releasesentinel.api.dto.ReleaseResponse;
import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import com.releasesentinel.repository.EnvironmentRepository;
import com.releasesentinel.repository.ProjectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReleaseTrackingService {

    private final ProjectRepository projectRepository;
    private final EnvironmentRepository environmentRepository;
    private final ReleaseRepository releaseRepository;

    public ReleaseTrackingService(
            ProjectRepository projectRepository,
            EnvironmentRepository environmentRepository,
            ReleaseRepository releaseRepository) {
        this.projectRepository = projectRepository;
        this.environmentRepository = environmentRepository;
        this.releaseRepository = releaseRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByKeyIgnoreCase(request.key())) {
            throw new DuplicateResourceException("Project key already exists: " + request.key());
        }

        Project project = new Project(request.key(), request.name(), request.description());
        return ProjectResponse.from(projectRepository.save(project));
    }

    public List<ProjectResponse> getProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse getProject(UUID projectId) {
        return ProjectResponse.from(findProject(projectId));
    }

    @Transactional
    public EnvironmentResponse createEnvironment(UUID projectId, CreateEnvironmentRequest request) {
        Project project = findProject(projectId);

        if (environmentRepository.existsByProjectIdAndNameIgnoreCase(projectId, request.name())) {
            throw new DuplicateResourceException("Environment already exists for project: " + request.name());
        }

        Environment environment = new Environment(project, request.name(), request.type(), request.baseUrl());
        return EnvironmentResponse.from(environmentRepository.save(environment));
    }

    public List<EnvironmentResponse> getProjectEnvironments(UUID projectId) {
        findProject(projectId);
        return environmentRepository.findByProjectIdOrderByNameAsc(projectId).stream()
                .map(EnvironmentResponse::from)
                .toList();
    }

    @Transactional
    public ReleaseResponse createRelease(UUID projectId, CreateReleaseRequest request) {
        Project project = findProject(projectId);

        if (releaseRepository.existsByProjectIdAndVersionIgnoreCase(projectId, request.version())) {
            throw new DuplicateResourceException("Release version already exists for project: " + request.version());
        }

        ReleaseStatus status = request.status() == null ? ReleaseStatus.PLANNED : request.status();
        Release release = new Release(project, request.version(), status, request.targetDate());
        return ReleaseResponse.from(releaseRepository.save(release));
    }

    public List<ReleaseResponse> getProjectReleases(UUID projectId) {
        findProject(projectId);
        return releaseRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(ReleaseResponse::from)
                .toList();
    }

    public ReleaseResponse getRelease(UUID releaseId) {
        return ReleaseResponse.from(releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found: " + releaseId)));
    }

    public Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
