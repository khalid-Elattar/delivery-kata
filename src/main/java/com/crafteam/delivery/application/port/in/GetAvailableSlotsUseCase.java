package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

/**
 * Input port for getting available delivery slots.
 */
public interface GetAvailableSlotsUseCase {
    Flux<Slot> execute(DeliveryMode mode, LocalDate date);

    Flux<Slot> executeAll();
}
