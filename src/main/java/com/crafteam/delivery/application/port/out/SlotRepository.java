package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output port for slot persistence operations.
 * Slots are now availability templates (not specific to a date).
 */
public interface SlotRepository {

    Mono<Slot> findById(SlotId id);

    /**
     * Find the slot template for a specific delivery mode.
     * There should be only one template per delivery mode.
     *
     * @param mode The delivery mode
     * @return Mono of the slot template if found, empty otherwise
     */
    Mono<Slot> findByDeliveryMode(DeliveryMode mode);

    Flux<Slot> findAll();

    Mono<Slot> save(Slot slot);

    Mono<Void> delete(SlotId id);
}
