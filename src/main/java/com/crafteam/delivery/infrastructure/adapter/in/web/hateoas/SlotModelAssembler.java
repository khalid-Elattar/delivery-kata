package com.crafteam.delivery.infrastructure.adapter.in.web.hateoas;

import com.crafteam.delivery.infrastructure.adapter.in.web.SlotController;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.response.SlotResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.ReactiveRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

/**
 * HATEOAS assembler for SlotResponse.
 */
@Component
public class SlotModelAssembler implements ReactiveRepresentationModelAssembler<SlotResponse, EntityModel<SlotResponse>> {

    @Override
    public Mono<EntityModel<SlotResponse>> toModel(SlotResponse entity, ServerWebExchange exchange) {
        return linkTo(methodOn(SlotController.class).getAllSlots())
                .withRel("slots")
                .toMono()
                .map(link -> EntityModel.of(entity, link));
    }

    public EntityModel<SlotResponse> toModel(SlotResponse entity) {
        return EntityModel.of(entity);
    }
}
