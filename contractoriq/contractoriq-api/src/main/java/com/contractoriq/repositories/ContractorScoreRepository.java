package com.contractoriq.repositories;

import com.contractoriq.models.ContractorScore;
import com.contractoriq.models.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractorScoreRepository extends JpaRepository<ContractorScore, UUID> {

    Optional<ContractorScore> findFirstByContractorIdOrderByCalculatedAtDesc(UUID contractorId);

    List<ContractorScore> findTop10ByOrderByOverallScoreDesc();

    List<ContractorScore> findByRecommendation(Recommendation recommendation);
}
