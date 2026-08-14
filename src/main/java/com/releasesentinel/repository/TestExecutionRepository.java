package com.releasesentinel.repository;

import com.releasesentinel.domain.TestExecution;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestExecutionRepository extends JpaRepository<TestExecution, UUID> {

    boolean existsByTestRunIdAndTestCaseId(UUID testRunId, UUID testCaseId);

    List<TestExecution> findByTestRunIdOrderByExecutedAtAsc(UUID testRunId);

    List<TestExecution> findByTestRunReleaseIdOrderByExecutedAtAsc(UUID releaseId);
}
