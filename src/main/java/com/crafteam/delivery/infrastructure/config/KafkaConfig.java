package com.crafteam.delivery.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration for event streaming.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic slotsTopic() {
        return TopicBuilder.name("delivery.slots")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingsTopic() {
        return TopicBuilder.name("delivery.bookings")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
