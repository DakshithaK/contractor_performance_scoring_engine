package com.contractoriq.controllers;

import com.contractoriq.cache.ScoreCacheService;
import com.contractoriq.dto.ContractorScoreDTO;
import com.contractoriq.models.Contractor;
import com.contractoriq.models.ContractorScore;
import com.contractoriq.repositories.ContractorRepository;
import com.contractoriq.repositories.ContractorScoreRepository;
import com.contractoriq.services.ContractorService;
import com.contractoriq.services.ScoringEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.contractoriq.models.Recommendation;

@RestController
@RequestMapping("/api/v1")
public class ScoreController {

    private final ScoringEngine scoringEngine;
    private final ContractorRepository contractorRepository;
    private final ContractorScoreRepository scoreRepository;
    private final ScoreCacheService scoreCacheService;
    private final ContractorService contractorService;

    public ScoreController(ScoringEngine scoringEngine,
                           ContractorRepository contractorRepository,
                           ContractorScoreRepository scoreRepository,
                           ScoreCacheService scoreCacheService,
                           ContractorService contractorService) {
        this.scoringEngine = scoringEngine;
        this.contractorRepository = contractorRepository;
        this.scoreRepository = scoreRepository;
        this.scoreCacheService = scoreCacheService;
        this.contractorService = contractorService;
    }

    @PostMapping("/scores/calculate/{id}")
    public ContractorScoreDTO calculate(@PathVariable UUID id) {
        ContractorScore score = scoringEngine.calculate(id);
        scoreCacheService.invalidate(id);
        return contractorService.getScoreDTO(id);
    }

    @PostMapping("/scores/calculate-all")
    public List<ContractorScoreDTO> calculateAll() {
        List<ContractorScoreDTO> out = new ArrayList<>();
        for (Contractor c : contractorRepository.findAll()) {
            scoringEngine.calculate(c.getId());
            scoreCacheService.invalidate(c.getId());
            out.add(contractorService.getScoreDTO(c.getId()));
        }
        return out;
    }

    @GetMapping("/leaderboard")
    public List<ContractorScoreDTO> leaderboard() {
        return contractorService.listAllWithScores().stream()
                .sorted(Comparator.comparing(ContractorScoreDTO::getOverallScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();
    }

    @GetMapping("/flagged")
    public List<ContractorScoreDTO> flagged() {
        return contractorService.listAllWithScores().stream()
                .filter(d -> d.getRecommendation() == Recommendation.AVOID)
                .toList();
    }
}
