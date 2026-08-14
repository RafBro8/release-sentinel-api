package com.releasesentinel.repository;

import com.releasesentinel.domain.TestRun;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRunRepository extends JpaRepository<TestRun, UUID> {

    List<TestRun> findByReleaseIdOrderByCreatedAtDesc(UUID releaseId);
}
