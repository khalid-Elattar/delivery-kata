package com.crafteam.delivery.infrastructure.adapter.out.messaging.kafka;

import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.domain.event.BookingCancelledEvent;
import com.crafteam.delivery.domain.event.BookingConfirmedEvent;
import com.crafteam.delivery.domain.event.SlotBookedEvent;
import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Reactive Kafka adapter for publishing domain events.
 * Uses reactor-kafka for non-blocking event publishing.
 *
 * Error Handling Strategy:
 * - Retries with exponential backoff (3 attempts)
 * - Logs failures with detailed context
 * - Gracefully handles errors without blocking reactive chain
 * - In production: consider implementing Dead Letter Queue (DLQ)
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private static final String SLOT_TOPIC = "delivery.slots";
    private static final String BOOKING_TOPIC = "delivery.bookings";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofMillis(100);
    private static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(2);

    private final KafkaSender<String, Object> kafkaSender;

    public KafkaEventPublisher(KafkaSender<String, Object> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Override
    public Mono<Void> publish(DomainEvent event) {
        String topic = resolveTopic(event);
        String key = resolveKey(event);

        log.info("Publishing event to {}: {}", topic, event.getClass().getSimpleName());

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        SenderRecord<String, Object, DomainEvent> senderRecord =
                SenderRecord.create(record, event);

        return kafkaSender.send(Mono.just(senderRecord))
                .doOnNext(result -> {
                    if (result.exception() != null) {
                        log.error("Failed to publish event on attempt: {}", event, result.exception());
                    } else {
                        log.debug("Event published successfully to partition {}, offset {}: {}",
                                result.recordMetadata().partition(),
                                result.recordMetadata().offset(),
                                event.getClass().getSimpleName());
                    }
                })
                .then()
                // Retry with exponential backoff
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_MIN_BACKOFF)
                        .maxBackoff(RETRY_MAX_BACKOFF)
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying event publish (attempt {}/{}): {}",
                                        retrySignal.totalRetries() + 1,
                                        MAX_RETRY_ATTEMPTS,
                                        event.getClass().getSimpleName()))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("CRITICAL: Event publish failed after {} retries: {} - Event: {}",
                                    MAX_RETRY_ATTEMPTS,
                                    event.getClass().getSimpleName(),
                                    event);
                            return retrySignal.failure();
                        }))
                // Last resort: log and continue (don't break the reactive chain)
                .onErrorResume(ex -> {
                    log.error("CRITICAL: Event permanently lost after all retries - Type: {}, Event: {}. " +
                              "Consider implementing Dead Letter Queue (DLQ) for production.",
                              event.getClass().getSimpleName(), event, ex);

                    // TODO: In production, send to DLQ instead of dropping
                    // deadLetterQueueService.save(event, ex);

                    return Mono.empty(); // Continue processing, don't fail the operation
                });
    }

    @Override
    public Mono<Void> publishAll(Iterable<DomainEvent> events) {
        return Flux.fromIterable(events)
                .flatMap(this::publish)
                .then();
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event) {
            case SlotCreatedEvent e -> SLOT_TOPIC;
            case SlotBookedEvent e -> SLOT_TOPIC;
            case BookingConfirmedEvent e -> BOOKING_TOPIC;
            case BookingCancelledEvent e -> BOOKING_TOPIC;
            default -> "delivery.events";
        };
    }

    private String resolveKey(DomainEvent event) {
        return switch (event) {
            case SlotCreatedEvent e -> e.slotId().toString();
            case SlotBookedEvent e -> e.slotId().toString();
            case BookingConfirmedEvent e -> e.bookingId().toString();
            case BookingCancelledEvent e -> e.bookingId().toString();
            default -> null;
        };
    }
}
