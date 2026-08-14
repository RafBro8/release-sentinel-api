package com.releasesentinel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.releasesentinel.api.dto.CreateDefectRequest;
import com.releasesentinel.api.dto.DefectResponse;
import com.releasesentinel.api.dto.UpdateDefectStatusRequest;
import com.releasesentinel.domain.Defect;
import com.releasesentinel.domain.DefectPriority;
import com.releasesentinel.domain.DefectSeverity;
import com.releasesentinel.domain.DefectStatus;
import com.releasesentinel.domain.Environment;
import com.releasesentinel.domain.EnvironmentType;
import com.releasesentinel.domain.Project;
import com.releasesentinel.domain.Release;
import com.releasesentinel.domain.ReleaseStatus;
import com.releasesentinel.domain.TestCase;
import com.releasesentinel.domain.TestExecution;
import com.releasesentinel.domain.TestPriority;
import com.releasesentinel.domain.TestResult;
import com.releasesentinel.domain.TestRun;
import com.releasesentinel.domain.TestType;
import com.releasesentinel.repository.DefectRepository;
import com.releasesentinel.repository.ReleaseRepository;
import com.releasesentinel.repository.TestExecutionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefectServiceTest {

    @Mock
    private ReleaseTrackingService releaseTrackingService;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private TestExecutionRepository testExecutionRepository;

    @Mock
    private DefectRepository defectRepository;

    @InjectMocks
    private DefectService defectService;

    @Test
    void createCriticalDefectDefaultsToBlockingRelease() {
        UUID releaseId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        CreateDefectRequest request = new CreateDefectRequest(
                "Checkout returns 500",
                "Payment confirmation fails",
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null,
                null);

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(defectRepository.save(any(Defect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DefectResponse response = defectService.createDefect(releaseId, request);

        assertThat(response.status()).isEqualTo(DefectStatus.OPEN);
        assertThat(response.blockingRelease()).isTrue();
        verify(defectRepository).save(any(Defect.class));
    }

    @Test
    void createHighDefectDefaultsToNotBlockingRelease() {
        UUID releaseId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        CreateDefectRequest request = new CreateDefectRequest(
                "Invoice total is incorrect",
                null,
                DefectSeverity.HIGH,
                DefectPriority.HIGH,
                null,
                null);

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(defectRepository.save(any(Defect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DefectResponse response = defectService.createDefect(releaseId, request);

        assertThat(response.blockingRelease()).isFalse();
    }

    @Test
    void createDefectRejectsLinkedExecutionFromDifferentRelease() {
        UUID releaseId = UUID.randomUUID();
        UUID linkedExecutionId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Release otherRelease = new Release(project, "1.1.0", ReleaseStatus.IN_TEST, null);
        Environment environment = new Environment(project, "staging", EnvironmentType.STAGING, null);
        TestRun otherRun = new TestRun(otherRelease, environment, "Regression run");
        TestCase testCase = new TestCase(project, "Checkout API", null, TestPriority.CRITICAL, TestType.API);
        TestExecution linkedExecution = new TestExecution(otherRun, testCase, TestResult.FAILED, null);
        CreateDefectRequest request = new CreateDefectRequest(
                "Checkout API fails",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null,
                linkedExecutionId);

        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(testExecutionRepository.findById(linkedExecutionId)).thenReturn(Optional.of(linkedExecution));

        assertThatThrownBy(() -> defectService.createDefect(releaseId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Linked test execution does not belong to the release");
    }

    @Test
    void updateStatusAllowsResolvedToClosed() {
        UUID defectId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Defect defect = new Defect(
                release,
                null,
                "Checkout API fails",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null);
        defect.updateStatus(DefectStatus.RESOLVED);

        when(defectRepository.findById(defectId)).thenReturn(Optional.of(defect));
        when(defectRepository.save(any(Defect.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DefectResponse response = defectService.updateDefectStatus(
                defectId,
                new UpdateDefectStatusRequest(DefectStatus.CLOSED));

        assertThat(response.status()).isEqualTo(DefectStatus.CLOSED);
    }

    @Test
    void updateStatusRejectsOpenToClosed() {
        UUID defectId = UUID.randomUUID();
        Project project = new Project("SHOP", "Shopping Platform", null);
        Release release = new Release(project, "1.0.0", ReleaseStatus.IN_TEST, null);
        Defect defect = new Defect(
                release,
                null,
                "Checkout API fails",
                null,
                DefectSeverity.CRITICAL,
                DefectPriority.URGENT,
                null);

        when(defectRepository.findById(defectId)).thenReturn(Optional.of(defect));

        assertThatThrownBy(() -> defectService.updateDefectStatus(
                        defectId,
                        new UpdateDefectStatusRequest(DefectStatus.CLOSED)))
                .isInstanceOf(InvalidDefectStatusTransitionException.class)
                .hasMessage("Defect must be resolved before it can be closed");
    }
}
