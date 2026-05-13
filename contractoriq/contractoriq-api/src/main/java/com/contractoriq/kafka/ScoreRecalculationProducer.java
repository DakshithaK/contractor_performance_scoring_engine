package com.contractoriq.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScoreRecalculationProducer {

    private static final Logger log = LoggerFactory.getLogger(ScoreRecalculationProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${contractoriq.kafka.topic.score-recalculation}")
    private String topic;

    public ScoreRecalculationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UUID contractorId, String trigger) {
        ScoreRecalculationEvent event = new ScoreRecalculationEvent(contractorId, trigger);
        try {
            kafkaTemplate.send(topic, contractorId.toString(), event);
            log.info("published score-recalculation event {} for {}", trigger, contractorId);
        } catch (Exception ex) {
            log.warn("kafka publish failed for {}: {}", contractorId, ex.getMessage());
        }
    }
}
