package com.releasesentinel.repository;

import com.releasesentinel.domain.TestCase;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    boolean existsByProjectIdAndTitleIgnoreCase(UUID projectId, String title);

    List<TestCase> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
