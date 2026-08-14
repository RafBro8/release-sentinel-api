package com.releasesentinel.repository;

import com.releasesentinel.domain.Defect;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefectRepository extends JpaRepository<Defect, UUID> {

    List<Defect> findByReleaseIdOrderByCreatedAtDesc(UUID releaseId);

    List<Defect> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
