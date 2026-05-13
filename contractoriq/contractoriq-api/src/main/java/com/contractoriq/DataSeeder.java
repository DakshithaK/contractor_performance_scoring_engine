package com.contractoriq;

import com.contractoriq.models.CompletionStatus;
import com.contractoriq.models.Contractor;
import com.contractoriq.models.Project;
import com.contractoriq.models.SitePhotoAnalysis;
import com.contractoriq.models.Trade;
import com.contractoriq.repositories.ContractorRepository;
import com.contractoriq.repositories.ProjectRepository;
import com.contractoriq.repositories.SitePhotoAnalysisRepository;
import com.contractoriq.services.ScoringEngine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ContractorRepository contractorRepository;
    private final ProjectRepository projectRepository;
    private final SitePhotoAnalysisRepository photoRepository;
    private final ScoringEngine scoringEngine;

    public DataSeeder(ContractorRepository contractorRepository,
                      ProjectRepository projectRepository,
                      SitePhotoAnalysisRepository photoRepository,
                      ScoringEngine scoringEngine) {
        this.contractorRepository = contractorRepository;
        this.projectRepository = projectRepository;
        this.photoRepository = photoRepository;
        this.scoringEngine = scoringEngine;
    }

    @PostConstruct
    public void seed() {
        if (contractorRepository.count() > 0) {
            log.info("seed skipped — contractors table already populated");
            return;
        }
        log.info("seeding demo contractors, projects and photo analyses...");

        Random rng = new Random(42);
        String[] cities = {"Bengaluru", "Mumbai", "Hyderabad", "Pune"};
        Trade[] trades = Trade.values();

        // Profile: name, city, trade, performance band (0..4: 0=excellent, 4=terrible)
        Object[][] roster = {
                {"Ramesh Builders",       "Bengaluru", Trade.CIVIL,      0},
                {"Sunrise Electricals",   "Bengaluru", Trade.ELECTRICAL, 1},
                {"Mumbai Plumbworks",     "Mumbai",    Trade.PLUMBING,   2},
                {"Vasai Finishing Co",    "Mumbai",    Trade.FINISHING,  3},
                {"Hyderabad Civil Co",    "Hyderabad", Trade.CIVIL,      1},
                {"Telangana Wires",       "Hyderabad", Trade.ELECTRICAL, 4},
                {"Pune Plumb Pros",       "Pune",      Trade.PLUMBING,   0},
                {"Deccan Finishes",       "Pune",      Trade.FINISHING,  2},
                {"Karnataka Concrete",    "Bengaluru", Trade.CIVIL,      4},
                {"Powai Wirelines",       "Mumbai",    Trade.ELECTRICAL, 0},
                {"Hitec Plumbing",        "Hyderabad", Trade.PLUMBING,   3},
                {"Kothrud Finishing",     "Pune",      Trade.FINISHING,  1},
        };

        for (Object[] row : roster) {
            String name = (String) row[0];
            String city = (String) row[1];
            Trade trade = (Trade) row[2];
            int band = (int) row[3];

            Contractor c = new Contractor(
                    UUID.randomUUID(), name, city, trade,
                    "+91" + (9000000000L + rng.nextInt(1_000_000)),
                    LocalDate.now().minusDays(180 + rng.nextInt(720)),
                    true);
            contractorRepository.save(c);

            int projectCount = 3 + rng.nextInt(6); // 3..8
            for (int i = 0; i < projectCount; i++) {
                projectRepository.save(buildProject(c.getId(), city, band, rng, i));
            }

            int photoCount = band == 4 ? 0 : 1 + rng.nextInt(3);
            for (int i = 0; i < photoCount; i++) {
                photoRepository.save(buildPhoto(c.getId(), band, rng));
            }
        }

        log.info("calculating initial scores for {} contractors...",
                contractorRepository.count());
        contractorRepository.findAll().forEach(c -> {
            try {
                scoringEngine.calculate(c.getId());
            } catch (Exception ex) {
                log.warn("initial score calc failed for {}: {}", c.getName(), ex.getMessage());
            }
        });
        log.info("seed complete.");
    }

    private Project buildProject(UUID contractorId, String city, int band, Random rng, int idx) {
        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setContractorId(contractorId);
        p.setProjectName("Project-" + (idx + 1));
        p.setCity(city);

        LocalDate start = LocalDate.now().minusDays(120 + idx * 30 + rng.nextInt(10));
        LocalDate plannedEnd = start.plusDays(45 + rng.nextInt(30));

        int delayDays = switch (band) {
            case 0 -> -rng.nextInt(4);              // a few days early
            case 1 -> rng.nextInt(4);               // mostly on time
            case 2 -> 5 + rng.nextInt(10);          // ~1-2 weeks late
            case 3 -> 15 + rng.nextInt(20);         // 2-5 weeks late
            default -> 30 + rng.nextInt(45);        // very late
        };
        LocalDate actualEnd = plannedEnd.plusDays(delayDays);

        BigDecimal planned = new BigDecimal(500_000 + rng.nextInt(1_000_000));
        double overrun = switch (band) {
            case 0 -> 0.95 + rng.nextDouble() * 0.05;  // under budget
            case 1 -> 1.00 + rng.nextDouble() * 0.08;
            case 2 -> 1.10 + rng.nextDouble() * 0.15;
            case 3 -> 1.30 + rng.nextDouble() * 0.20;
            default -> 1.50 + rng.nextDouble() * 0.30;
        };
        BigDecimal actual = planned.multiply(BigDecimal.valueOf(overrun))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        int rating = switch (band) {
            case 0 -> 5;
            case 1 -> 4 + rng.nextInt(2);
            case 2 -> 3 + rng.nextInt(2);
            case 3 -> 2 + rng.nextInt(2);
            default -> 1 + rng.nextInt(2);
        };

        p.setStartDate(start);
        p.setEndDate(actualEnd);
        p.setPlannedEndDate(plannedEnd);
        p.setBudgetPlanned(planned);
        p.setBudgetActual(actual);
        p.setCustomerRating(rating);
        p.setCompletionStatus(CompletionStatus.COMPLETED);
        return p;
    }

    private SitePhotoAnalysis buildPhoto(UUID contractorId, int band, Random rng) {
        SitePhotoAnalysis a = new SitePhotoAnalysis();
        a.setId(UUID.randomUUID());
        a.setContractorId(contractorId);
        a.setPhotoPath("seed://placeholder.jpg");
        int quality = switch (band) {
            case 0 -> 85 + rng.nextInt(15);
            case 1 -> 75 + rng.nextInt(15);
            case 2 -> 60 + rng.nextInt(15);
            case 3 -> 40 + rng.nextInt(20);
            default -> 20 + rng.nextInt(20);
        };
        a.setQualityScore(quality);
        a.setIssuesFound(List.of());
        a.setClaudeRawResponse(null);
        a.setAnalyzedAt(Instant.now());
        return a;
    }
}
