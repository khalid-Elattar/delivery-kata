package com.crafteam.delivery.infrastructure.adapter.out.messaging.kafka;

import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.domain.event.BookingCancelledEvent;
import com.crafteam.delivery.domain.event.BookingConfirmedEvent;
import com.crafteam.delivery.domain.event.SlotBookedEvent;
import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.model.shared.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Kafka adapter for publishing domain events.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private static final String SLOT_TOPIC = "delivery.slots";
    private static final String BOOKING_TOPIC = "delivery.bookings";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(DomainEvent event) {
        return Mono.fromRunnable(() -> {
            String topic = resolveTopic(event);
            String key = resolveKey(event);

            log.info("Publishing event to {}: {}", topic, event.getClass().getSimpleName());

            kafkaTemplate.send(topic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {}", event, ex);
                        } else {
                            log.debug("Event published successfully: {}", event.getClass().getSimpleName());
                        }
                    });
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
