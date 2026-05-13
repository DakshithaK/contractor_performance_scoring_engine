package com.contractoriq.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${contractoriq.kafka.topic.score-recalculation}")
    private String scoreRecalculationTopic;

    @Bean
    public NewTopic scoreRecalculationTopic() {
        return new NewTopic(scoreRecalculationTopic, 1, (short) 1);
    }
}
