package com.releasesentinel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.releasesentinel.api.dto.CreateEnvironmentRequest;
import com.releasesentinel.api.dto.CreateProjectRequest;
import com.releasesentinel.api.dto.CreateReleaseRequest;
import com.releasesentinel.api.dto.ProjectResponse;
import com.releasesentinel.api.dto.ReleaseResponse;
import com.releasesentinel.domain.EnvironmentType;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import com.releasesentinel.repository.EnvironmentRepository;
import com.releasesentinel.repository.ProjectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReleaseTrackingServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @InjectMocks
    private ReleaseTrackingService releaseTrackingService;

    @Test
    void createProjectRejectsDuplicateProjectKey() {
        CreateProjectRequest request = new CreateProjectRequest("SHOP", "Shopping Platform", null);
        when(projectRepository.existsByKeyIgnoreCase("SHOP")).thenReturn(true);

        assertThatThrownBy(() -> releaseTrackingService.createProject(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Project key already exists: SHOP");
    }

    @Test
    void createProjectSavesNewProject() {
        CreateProjectRequest request = new CreateProjectRequest("SHOP", "Shopping Platform", null);
        when(projectRepository.existsByKeyIgnoreCase("SHOP")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = releaseTrackingService.createProject(request);

        assertThat(response.key()).isEqualTo("SHOP");
        assertThat(response.name()).isEqualTo("Shopping Platform");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createEnvironmentRejectsMissingProject() {
        UUID projectId = UUID.randomUUID();
        CreateEnvironmentRequest request = new CreateEnvironmentRequest(
                "staging",
                EnvironmentType.STAGING,
                "https://staging.example.com");
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> releaseTrackingService.createEnvironment(projectId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: " + projectId);
    }

    @Test
    void createReleaseDefaultsToPlannedStatus() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        CreateReleaseRequest request = new CreateReleaseRequest(
                "1.0.0",
                null,
                LocalDate.parse("2026-09-01"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(releaseRepository.existsByProjectIdAndVersionIgnoreCase(projectId, "1.0.0")).thenReturn(false);
        when(releaseRepository.save(any(Release.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReleaseResponse response = releaseTrackingService.createRelease(projectId, request);

        assertThat(response.version()).isEqualTo("1.0.0");
        assertThat(response.status()).isEqualTo(ReleaseStatus.PLANNED);
        assertThat(response.targetDate()).isEqualTo(LocalDate.parse("2026-09-01"));
    }
}
