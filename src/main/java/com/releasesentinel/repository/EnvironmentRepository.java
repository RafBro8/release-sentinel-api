package com.releasesentinel.repository;

import com.releasesentinel.domain.Environment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {

    boolean existsByProjectIdAndNameIgnoreCase(UUID projectId, String name);

    List<Environment> findByProjectIdOrderByNameAsc(UUID projectId);
}
