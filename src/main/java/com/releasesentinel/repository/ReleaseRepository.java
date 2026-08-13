package com.releasesentinel.repository;

import com.releasesentinel.domain.Release;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseRepository extends JpaRepository<Release, UUID> {

    boolean existsByProjectIdAndVersionIgnoreCase(UUID projectId, String version);

    List<Release> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
