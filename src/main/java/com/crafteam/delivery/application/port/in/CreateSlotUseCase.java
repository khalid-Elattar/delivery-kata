package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.domain.model.slot.Slot;
import reactor.core.publisher.Mono;

/**
 * Input port for creating a new delivery slot.
 */
public interface CreateSlotUseCase {
    Mono<Slot> execute(CreateSlotCommand command);
}
