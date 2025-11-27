package com.crafteam.delivery.application.port.in;

import com.crafteam.delivery.application.dto.query.SlotAvailabilityQuery;
import com.crafteam.delivery.application.dto.response.SlotAvailabilityResponse;
import reactor.core.publisher.Mono;

/**
 * Use case for getting slot availability for a specific mode and date.
 */
public interface GetSlotAvailabilityUseCase {

    Mono<SlotAvailabilityResponse> execute(SlotAvailabilityQuery query);
}
