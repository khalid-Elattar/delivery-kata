package com.crafteam.delivery.domain.model.shared;

import java.time.Instant;

/**
 * Base interface for all domain events.
 */
public interface DomainEvent {
    Instant occurredAt();
}
