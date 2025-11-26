package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.shared.DomainEvent;
import reactor.core.publisher.Mono;

/**
 * Output port for publishing domain events.
 */
public interface EventPublisher {

    Mono<Void> publish(DomainEvent event);

    Mono<Void> publishAll(Iterable<DomainEvent> events);
}
