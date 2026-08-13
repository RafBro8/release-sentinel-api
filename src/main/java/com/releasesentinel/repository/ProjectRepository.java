package com.releasesentinel.repository;

import com.releasesentinel.domain.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByKeyIgnoreCase(String key);
}
