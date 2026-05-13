package com.contractoriq.services;

import com.contractoriq.cache.ScoreCacheService;
import com.contractoriq.dto.ContractorScoreDTO;
import com.contractoriq.models.Contractor;
import com.contractoriq.models.ContractorScore;
import com.contractoriq.models.Recommendation;
import com.contractoriq.repositories.ContractorRepository;
import com.contractoriq.repositories.ContractorScoreRepository;
import com.contractoriq.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContractorService {

    private final ContractorRepository contractorRepository;
    private final ContractorScoreRepository scoreRepository;
    private final ProjectRepository projectRepository;
    private final ScoreCacheService scoreCacheService;

    public ContractorService(ContractorRepository contractorRepository,
                             ContractorScoreRepository scoreRepository,
                             ProjectRepository projectRepository,
                             ScoreCacheService scoreCacheService) {
        this.contractorRepository = contractorRepository;
        this.scoreRepository = scoreRepository;
        this.projectRepository = projectRepository;
        this.scoreCacheService = scoreCacheService;
    }

    public Contractor create(Contractor contractor) {
        if (contractor.getId() == null) {
            contractor.setId(UUID.randomUUID());
        }
        return contractorRepository.save(contractor);
    }

    public List<Contractor> findAll() {
        return contractorRepository.findAll();
    }

    public Optional<Contractor> findById(UUID id) {
        return contractorRepository.findById(id);
    }

    public ContractorScoreDTO getScoreDTO(UUID contractorId) {
        ContractorScoreDTO cached = scoreCacheService.get(contractorId);
        if (cached != null) {
            return cached;
        }
        Contractor contractor = contractorRepository.findById(contractorId).orElse(null);
        if (contractor == null) {
            return null;
        }
        Optional<ContractorScore> latest = scoreRepository
                .findFirstByContractorIdOrderByCalculatedAtDesc(contractorId);
        ContractorScoreDTO dto = toDTO(contractor, latest.orElse(null));
        scoreCacheService.put(contractorId, dto);
        return dto;
    }

    public List<ContractorScoreDTO> listAllWithScores() {
        List<ContractorScoreDTO> result = new ArrayList<>();
        for (Contractor c : contractorRepository.findAll()) {
            ContractorScoreDTO dto = scoreCacheService.get(c.getId());
            if (dto == null) {
                Optional<ContractorScore> score = scoreRepository
                        .findFirstByContractorIdOrderByCalculatedAtDesc(c.getId());
                dto = toDTO(c, score.orElse(null));
                scoreCacheService.put(c.getId(), dto);
            }
            result.add(dto);
        }
        return result;
    }

    public ContractorScoreDTO toDTO(Contractor c, ContractorScore score) {
        ContractorScoreDTO dto = new ContractorScoreDTO();
        dto.setContractorId(c.getId());
        dto.setName(c.getName());
        dto.setCity(c.getCity());
        dto.setTrade(c.getTrade());
        dto.setProjectCount(projectRepository.findByContractorId(c.getId()).size());
        if (score != null) {
            dto.setCalculatedAt(score.getCalculatedAt());
            dto.setDelayScore(score.getDelayScore());
            dto.setBudgetScore(score.getBudgetScore());
            dto.setQualityScore(score.getQualityScore());
            dto.setCustomerScore(score.getCustomerScore());
            dto.setOverallScore(score.getOverallScore());
            dto.setRecommendation(score.getRecommendation());
            dto.setRecommendationReasoning(score.getRecommendationReasoning());
        } else {
            dto.setOverallScore(BigDecimal.ZERO);
            dto.setRecommendation(Recommendation.CAUTION);
        }
        return dto;
    }
}
