package com.releasesentinel.service;

import com.releasesentinel.api.dto.CreateDefectRequest;
import com.releasesentinel.api.dto.DefectResponse;
import com.releasesentinel.api.dto.UpdateDefectStatusRequest;
import com.releasesentinel.domain.Defect;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.repository.DefectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DefectService {

    private final ReleaseTrackingService releaseTrackingService;
    private final ReleaseRepository releaseRepository;
    private final TestExecutionRepository testExecutionRepository;
    private final DefectRepository defectRepository;

    public DefectService(
            ReleaseTrackingService releaseTrackingService,
            ReleaseRepository releaseRepository,
            TestExecutionRepository testExecutionRepository,
            DefectRepository defectRepository) {
        this.releaseTrackingService = releaseTrackingService;
        this.releaseRepository = releaseRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.defectRepository = defectRepository;
    }

    @Transactional
    public DefectResponse createDefect(UUID releaseId, CreateDefectRequest request) {
        Release release = findRelease(releaseId);
        TestExecution linkedExecution = findLinkedExecution(request.linkedTestExecutionId());

        if (linkedExecution != null && !linkedExecution.getTestRun().getRelease().getId().equals(releaseId)) {
            throw new DuplicateResourceException("Linked test execution does not belong to the release");
        }

        Defect defect = new Defect(
                release,
                linkedExecution,
                request.title(),
                request.description(),
                request.severity(),
                request.priority(),
                request.blockingRelease());

        return DefectResponse.from(defectRepository.save(defect));
    }

    public List<DefectResponse> getReleaseDefects(UUID releaseId) {
        findRelease(releaseId);
        return defectRepository.findByReleaseIdOrderByCreatedAtDesc(releaseId).stream()
                .map(DefectResponse::from)
                .toList();
    }

    public List<DefectResponse> getProjectDefects(UUID projectId) {
        releaseTrackingService.findProject(projectId);
        return defectRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(DefectResponse::from)
                .toList();
    }

    public DefectResponse getDefect(UUID defectId) {
        return DefectResponse.from(findDefect(defectId));
    }

    @Transactional
    public DefectResponse updateDefectStatus(UUID defectId, UpdateDefectStatusRequest request) {
        Defect defect = findDefect(defectId);
        try {
            defect.updateStatus(request.status());
        } catch (IllegalStateException ex) {
            throw new InvalidDefectStatusTransitionException(ex.getMessage());
        }

        return DefectResponse.from(defectRepository.save(defect));
    }

    private Release findRelease(UUID releaseId) {
        return releaseRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found: " + releaseId));
    }

    private Defect findDefect(UUID defectId) {
        return defectRepository.findById(defectId)
                .orElseThrow(() -> new ResourceNotFoundException("Defect not found: " + defectId));
    }

    private TestExecution findLinkedExecution(UUID linkedTestExecutionId) {
        if (linkedTestExecutionId == null) {
            return null;
        }

        return testExecutionRepository.findById(linkedTestExecutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Test execution not found: " + linkedTestExecutionId));
    }
}
