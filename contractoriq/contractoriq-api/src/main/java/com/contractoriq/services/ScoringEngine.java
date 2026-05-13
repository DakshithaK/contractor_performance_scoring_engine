package com.contractoriq.services;

import com.contractoriq.models.CompletionStatus;
import com.contractoriq.models.Contractor;
import com.contractoriq.models.ContractorScore;
import com.contractoriq.models.Project;
import com.contractoriq.models.Recommendation;
import com.contractoriq.models.SitePhotoAnalysis;
import com.contractoriq.repositories.ContractorRepository;
import com.contractoriq.repositories.ContractorScoreRepository;
import com.contractoriq.repositories.ProjectRepository;
import com.contractoriq.repositories.SitePhotoAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ScoringEngine {

    static final BigDecimal WEIGHT_DELAY    = new BigDecimal("0.25");
    static final BigDecimal WEIGHT_BUDGET   = new BigDecimal("0.25");
    static final BigDecimal WEIGHT_QUALITY  = new BigDecimal("0.30");
    static final BigDecimal WEIGHT_CUSTOMER = new BigDecimal("0.20");

    static final BigDecimal HUNDRED = new BigDecimal("100");
    static final BigDecimal ZERO    = BigDecimal.ZERO;

    private final ContractorRepository contractorRepository;
    private final ProjectRepository projectRepository;
    private final SitePhotoAnalysisRepository photoRepository;
    private final ContractorScoreRepository scoreRepository;
    private final RecommendationService recommendationService;

    public ScoringEngine(ContractorRepository contractorRepository,
                         ProjectRepository projectRepository,
                         SitePhotoAnalysisRepository photoRepository,
                         ContractorScoreRepository scoreRepository,
                         RecommendationService recommendationService) {
        this.contractorRepository = contractorRepository;
        this.projectRepository = projectRepository;
        this.photoRepository = photoRepository;
        this.scoreRepository = scoreRepository;
        this.recommendationService = recommendationService;
    }

    public static class ScoreResult {
        public BigDecimal delayScore;
        public BigDecimal budgetScore;
        public BigDecimal qualityScore;
        public BigDecimal customerScore;
        public BigDecimal overallScore;
        public Recommendation recommendation;
        public boolean qualityUnverified;
        public int projectCount;
    }

    @Transactional
    public ContractorScore calculate(UUID contractorId) {
        Contractor contractor = contractorRepository.findById(contractorId)
                .orElseThrow(() -> new NoSuchElementException("contractor not found: " + contractorId));

        List<Project> completed = projectRepository
                .findByContractorIdAndCompletionStatus(contractorId, CompletionStatus.COMPLETED);
        List<SitePhotoAnalysis> photos = photoRepository.findByContractorId(contractorId);

        ScoreResult r = computeScores(completed, photos);

        String reasoning = recommendationService.generateReasoning(contractor, r);

        ContractorScore score = new ContractorScore();
        score.setId(UUID.randomUUID());
        score.setContractorId(contractorId);
        score.setCalculatedAt(Instant.now());
        score.setDelayScore(r.delayScore);
        score.setBudgetScore(r.budgetScore);
        score.setQualityScore(r.qualityScore);
        score.setCustomerScore(r.customerScore);
        score.setOverallScore(r.overallScore);
        score.setRecommendation(r.recommendation);
        score.setRecommendationReasoning(reasoning);
        return scoreRepository.save(score);
    }

    public ScoreResult computeScores(List<Project> completedProjects,
                                     List<SitePhotoAnalysis> photos) {
        ScoreResult r = new ScoreResult();
        r.delayScore    = delayScore(completedProjects);
        r.budgetScore   = budgetScore(completedProjects);
        r.qualityScore  = qualityScore(photos, r);
        r.customerScore = customerScore(completedProjects);
        r.overallScore  = overallScore(r);
        r.recommendation = decideRecommendation(r);
        r.projectCount  = completedProjects.size();
        return r;
    }

    BigDecimal delayScore(List<Project> projects) {
        if (projects.isEmpty()) {
            return HUNDRED;
        }
        BigDecimal sum = ZERO;
        int count = 0;
        for (Project p : projects) {
            if (p.getEndDate() == null || p.getPlannedEndDate() == null) {
                continue;
            }
            long daysLate = ChronoUnit.DAYS.between(p.getPlannedEndDate(), p.getEndDate());
            BigDecimal projectScore;
            if (daysLate <= 0) {
                projectScore = HUNDRED;
            } else {
                BigDecimal weeksLate = new BigDecimal(daysLate)
                        .divide(new BigDecimal("7"), 4, RoundingMode.HALF_UP);
                BigDecimal penalty = weeksLate.multiply(new BigDecimal("10"));
                projectScore = HUNDRED.subtract(penalty);
                if (projectScore.compareTo(ZERO) < 0) {
                    projectScore = ZERO;
                }
            }
            sum = sum.add(projectScore);
            count++;
        }
        if (count == 0) return HUNDRED;
        return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    BigDecimal budgetScore(List<Project> projects) {
        if (projects.isEmpty()) return HUNDRED;
        BigDecimal sum = ZERO;
        int count = 0;
        for (Project p : projects) {
            if (p.getBudgetPlanned() == null || p.getBudgetActual() == null
                    || p.getBudgetActual().compareTo(ZERO) == 0) {
                continue;
            }
            BigDecimal ratio = p.getBudgetPlanned()
                    .divide(p.getBudgetActual(), 6, RoundingMode.HALF_UP)
                    .multiply(HUNDRED);
            if (ratio.compareTo(HUNDRED) > 0) {
                ratio = HUNDRED;
            }
            if (ratio.compareTo(ZERO) < 0) {
                ratio = ZERO;
            }
            sum = sum.add(ratio);
            count++;
        }
        if (count == 0) return HUNDRED;
        return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    BigDecimal qualityScore(List<SitePhotoAnalysis> photos, ScoreResult r) {
        if (photos.isEmpty()) {
            r.qualityUnverified = true;
            return new BigDecimal("70.00");
        }
        long total = 0;
        for (SitePhotoAnalysis p : photos) {
            total += p.getQualityScore();
        }
        return new BigDecimal(total)
                .divide(new BigDecimal(photos.size()), 2, RoundingMode.HALF_UP);
    }

    BigDecimal customerScore(List<Project> projects) {
        if (projects.isEmpty()) return ZERO;
        BigDecimal sum = ZERO;
        int count = 0;
        for (Project p : projects) {
            if (p.getCustomerRating() == null) continue;
            sum = sum.add(new BigDecimal(p.getCustomerRating()));
            count++;
        }
        if (count == 0) return ZERO;
        BigDecimal avg = sum.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
        return avg.multiply(new BigDecimal("20")).setScale(2, RoundingMode.HALF_UP);
    }

    BigDecimal overallScore(ScoreResult r) {
        BigDecimal overall = r.delayScore.multiply(WEIGHT_DELAY)
                .add(r.budgetScore.multiply(WEIGHT_BUDGET))
                .add(r.qualityScore.multiply(WEIGHT_QUALITY))
                .add(r.customerScore.multiply(WEIGHT_CUSTOMER));
        return overall.setScale(2, RoundingMode.HALF_UP);
    }

    Recommendation decideRecommendation(ScoreResult r) {
        BigDecimal overall = r.overallScore;
        BigDecimal minDim = min(r.delayScore, r.budgetScore, r.qualityScore, r.customerScore);

        if (overall.compareTo(new BigDecimal("50")) < 0
                || minDim.compareTo(new BigDecimal("30")) < 0) {
            return Recommendation.AVOID;
        }
        if (overall.compareTo(new BigDecimal("75")) >= 0
                && minDim.compareTo(new BigDecimal("50")) >= 0) {
            return Recommendation.HIRE;
        }
        return Recommendation.CAUTION;
    }

    private static BigDecimal min(BigDecimal... values) {
        BigDecimal m = values[0];
        for (BigDecimal v : values) {
            if (v.compareTo(m) < 0) m = v;
        }
        return m;
    }
}
