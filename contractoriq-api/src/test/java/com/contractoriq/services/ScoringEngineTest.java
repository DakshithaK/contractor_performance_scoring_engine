package com.contractoriq.services;

import com.contractoriq.models.CompletionStatus;
import com.contractoriq.models.Contractor;
import com.contractoriq.models.ContractorScore;
import com.contractoriq.models.Project;
import com.contractoriq.models.Recommendation;
import com.contractoriq.models.SitePhotoAnalysis;
import com.contractoriq.models.Trade;
import com.contractoriq.repositories.ContractorRepository;
import com.contractoriq.repositories.ContractorScoreRepository;
import com.contractoriq.repositories.ProjectRepository;
import com.contractoriq.repositories.SitePhotoAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoringEngineTest {

    @Mock ContractorRepository contractorRepository;
    @Mock ProjectRepository projectRepository;
    @Mock SitePhotoAnalysisRepository photoRepository;
    @Mock ContractorScoreRepository scoreRepository;
    @Mock RecommendationService recommendationService;

    @InjectMocks ScoringEngine engine;

    private UUID contractorId;
    private Contractor contractor;

    @BeforeEach
    void setUp() {
        contractorId = UUID.randomUUID();
        contractor = new Contractor(contractorId, "ACME Builders",
                "Bengaluru", Trade.CIVIL, "+919000000000",
                LocalDate.of(2023, 1, 1), true);
    }

    private Project project(LocalDate plannedEnd, LocalDate actualEnd,
                            BigDecimal planned, BigDecimal actual, int rating) {
        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setContractorId(contractorId);
        p.setProjectName("p");
        p.setCity("Bengaluru");
        p.setStartDate(plannedEnd.minusMonths(2));
        p.setEndDate(actualEnd);
        p.setPlannedEndDate(plannedEnd);
        p.setBudgetPlanned(planned);
        p.setBudgetActual(actual);
        p.setCustomerRating(rating);
        p.setCompletionStatus(CompletionStatus.COMPLETED);
        return p;
    }

    private SitePhotoAnalysis photo(int qualityScore) {
        SitePhotoAnalysis p = new SitePhotoAnalysis();
        p.setId(UUID.randomUUID());
        p.setContractorId(contractorId);
        p.setPhotoPath("/tmp/x.jpg");
        p.setQualityScore(qualityScore);
        return p;
    }

    private void stubCalculation(List<Project> completed, List<SitePhotoAnalysis> photos) {
        when(contractorRepository.findById(contractorId)).thenReturn(Optional.of(contractor));
        when(projectRepository.findByContractorIdAndCompletionStatus(eq(contractorId),
                eq(CompletionStatus.COMPLETED))).thenReturn(completed);
        when(photoRepository.findByContractorId(contractorId)).thenReturn(photos);
        when(recommendationService.generateReasoning(any(), any())).thenReturn("ok");
        when(scoreRepository.save(any(ContractorScore.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testPerfectContractorScores100() {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // delivered early, under budget, 5-star
            projects.add(project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 5, 25),
                    new BigDecimal("100000"), new BigDecimal("90000"), 5));
        }
        List<SitePhotoAnalysis> photos = List.of(photo(100), photo(100));
        stubCalculation(projects, photos);

        ContractorScore score = engine.calculate(contractorId);

        assertThat(score.getDelayScore()).isEqualByComparingTo("100.00");
        assertThat(score.getBudgetScore()).isEqualByComparingTo("100.00");
        assertThat(score.getQualityScore()).isEqualByComparingTo("100.00");
        assertThat(score.getCustomerScore()).isEqualByComparingTo("100.00");
        assertThat(score.getOverallScore()).isEqualByComparingTo("100.00");
        assertThat(score.getRecommendation()).isEqualTo(Recommendation.HIRE);
    }

    @Test
    void testLateProjectReducesDelayScore() {
        // 14 days late = 2 weeks = -20 → 80
        Project late = project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 15),
                new BigDecimal("100000"), new BigDecimal("100000"), 5);
        stubCalculation(List.of(late), List.of(photo(80)));

        ContractorScore score = engine.calculate(contractorId);

        assertThat(score.getDelayScore()).isEqualByComparingTo("80.00");
    }

    @Test
    void testOverBudgetReducesBudgetScore() {
        // planned 100k, actual 130k → 100/130*100 = ~76.92
        Project over = project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 1),
                new BigDecimal("100000"), new BigDecimal("130000"), 4);
        stubCalculation(List.of(over), List.of(photo(80)));

        ContractorScore score = engine.calculate(contractorId);

        assertThat(score.getBudgetScore()).isLessThan(new BigDecimal("80"));
        assertThat(score.getBudgetScore()).isGreaterThan(new BigDecimal("70"));
    }

    @Test
    void testNoPhotosDefaultsTo70WithUnverifiedFlag() {
        Project p = project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 1),
                new BigDecimal("100000"), new BigDecimal("100000"), 4);

        ScoringEngine.ScoreResult result = engine.computeScores(
                List.of(p), Collections.emptyList());

        assertThat(result.qualityScore).isEqualByComparingTo("70.00");
        assertThat(result.qualityUnverified).isTrue();
    }

    @Test
    void testAvoidRecommendationWhenScoreBelow50() {
        // Very late + very over budget + mediocre quality + bad ratings
        Project bad = project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 9, 1),
                new BigDecimal("100000"), new BigDecimal("200000"), 1);
        stubCalculation(List.of(bad), List.of(photo(20)));

        ContractorScore score = engine.calculate(contractorId);

        assertThat(score.getOverallScore()).isLessThan(new BigDecimal("50"));
        assertThat(score.getRecommendation()).isEqualTo(Recommendation.AVOID);
    }

    @Test
    void testHireRecommendationWhenAllDimensionsAbove75() {
        Project good = project(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 1),
                new BigDecimal("100000"), new BigDecimal("100000"), 5);
        stubCalculation(List.of(good), List.of(photo(85)));

        ContractorScore score = engine.calculate(contractorId);

        assertThat(score.getDelayScore()).isGreaterThanOrEqualTo(new BigDecimal("75"));
        assertThat(score.getBudgetScore()).isGreaterThanOrEqualTo(new BigDecimal("75"));
        assertThat(score.getQualityScore()).isGreaterThanOrEqualTo(new BigDecimal("75"));
        assertThat(score.getCustomerScore()).isGreaterThanOrEqualTo(new BigDecimal("75"));
        assertThat(score.getOverallScore()).isGreaterThanOrEqualTo(new BigDecimal("75"));
        assertThat(score.getRecommendation()).isEqualTo(Recommendation.HIRE);
    }
}
