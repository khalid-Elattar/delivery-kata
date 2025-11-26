package com.crafteam.delivery.interfaces.rest.hateoas;

import com.crafteam.delivery.interfaces.rest.BookingController;
import com.crafteam.delivery.interfaces.rest.dto.response.BookingResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.ReactiveRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

/**
 * HATEOAS assembler for BookingResponse.
 */
@Component
public class BookingModelAssembler implements ReactiveRepresentationModelAssembler<BookingResponse, EntityModel<BookingResponse>> {

    @Override
    public Mono<EntityModel<BookingResponse>> toModel(BookingResponse entity, ServerWebExchange exchange) {
        return Mono.zip(
                linkTo(methodOn(BookingController.class).getBooking(entity.id()))
                        .withSelfRel()
                        .toMono(),
                linkTo(methodOn(BookingController.class).cancelBooking(entity.id()))
                        .withRel("cancel")
                        .toMono()
        ).map(links -> EntityModel.of(entity, links.getT1(), links.getT2()));
    }

    public EntityModel<BookingResponse> toModel(BookingResponse entity) {
        return EntityModel.of(entity);
    }
}
