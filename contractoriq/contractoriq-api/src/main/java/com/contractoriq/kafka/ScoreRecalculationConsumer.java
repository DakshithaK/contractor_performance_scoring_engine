package com.contractoriq.kafka;

import com.contractoriq.cache.ScoreCacheService;
import com.contractoriq.services.ScoringEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ScoreRecalculationConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScoreRecalculationConsumer.class);

    private final ScoringEngine scoringEngine;
    private final ScoreCacheService scoreCacheService;

    public ScoreRecalculationConsumer(ScoringEngine scoringEngine,
                                      ScoreCacheService scoreCacheService) {
        this.scoringEngine = scoringEngine;
        this.scoreCacheService = scoreCacheService;
    }

    @KafkaListener(
            topics = "${contractoriq.kafka.topic.score-recalculation}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onEvent(ScoreRecalculationEvent event) {
        if (event == null || event.getContractorId() == null) {
            log.warn("dropping malformed score-recalculation event: {}", event);
            return;
        }
        log.info("consuming score-recalculation event {} for {}",
                event.getTrigger(), event.getContractorId());
        try {
            scoringEngine.calculate(event.getContractorId());
            scoreCacheService.invalidate(event.getContractorId());
        } catch (Exception ex) {
            log.error("score recalculation failed for {}: {}",
                    event.getContractorId(), ex.getMessage(), ex);
        }
    }
}
