package com.crafteam.delivery.infrastructure.config;

import com.crafteam.delivery.domain.event.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for event streaming with reactive producer and standard consumer support.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // ========== TOPICS ==========

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

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name("delivery.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ========== PRODUCER (Reactive) ==========

    /**
     * Reactive Kafka sender for publishing events.
     */
    @Bean
    public KafkaSender<String, Object> kafkaSender() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        SenderOptions<String, Object> senderOptions = SenderOptions.create(producerProps);
        return KafkaSender.create(senderOptions);
    }

    // ========== CONSUMER (Standard) ==========

    /**
     * Consumer factory with JSON deserialization.
     * Trusts all packages for deserialization (adjust in production for security).
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.crafteam.delivery.domain.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.crafteam.delivery.domain.model.shared.DomainEvent");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka listener container factory for event consumers.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 concurrent consumers per listener
        return factory;
    }

    // ========== EVENT FILTERS ==========

    /**
     * Filter for BookingConfirmedEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> bookingConfirmedFilter() {
        return record -> !(record.value() instanceof BookingConfirmedEvent);
    }

    /**
     * Filter for BookingCancelledEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> bookingCancelledFilter() {
        return record -> !(record.value() instanceof BookingCancelledEvent);
    }

    /**
     * Filter for SlotCreatedEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> slotCreatedFilter() {
        return record -> !(record.value() instanceof SlotCreatedEvent);
    }

    /**
     * Filter for SlotBookedEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> slotBookedFilter() {
        return record -> !(record.value() instanceof SlotBookedEvent);
    }

    /**
     * Filter for UserRegisteredEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> userRegisteredFilter() {
        return record -> !(record.value() instanceof UserRegisteredEvent);
    }

    /**
     * Filter for UserUpdatedEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> userUpdatedFilter() {
        return record -> !(record.value() instanceof UserUpdatedEvent);
    }

    /**
     * Filter for UserDeactivatedEvent
     */
    @Bean
    public org.springframework.kafka.listener.adapter.RecordFilterStrategy<String, Object> userDeactivatedFilter() {
        return record -> !(record.value() instanceof UserDeactivatedEvent);
    }
}
