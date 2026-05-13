package com.contractoriq.repositories;

import com.contractoriq.models.SitePhotoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SitePhotoAnalysisRepository extends JpaRepository<SitePhotoAnalysis, UUID> {

    List<SitePhotoAnalysis> findByContractorId(UUID contractorId);
}
