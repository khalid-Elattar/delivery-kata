package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.BookSlotCommand;
import com.crafteam.delivery.application.dto.command.CancelBookingCommand;
import com.crafteam.delivery.application.port.in.BookSlotUseCase;
import com.crafteam.delivery.application.port.in.CancelBookingUseCase;
import com.crafteam.delivery.application.port.in.GetBookingUseCase;
import com.crafteam.delivery.domain.exception.BookingNotFoundException;
import com.crafteam.delivery.domain.exception.SlotNotAvailableException;
import com.crafteam.delivery.domain.exception.SlotNotFoundException;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.booking.CustomerId;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.interfaces.rest.dto.request.BookSlotRequest;
import com.crafteam.delivery.interfaces.rest.hateoas.BookingModelAssembler;
import com.crafteam.delivery.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(BookingController.class)
@Import({SecurityConfig.class, BookingModelAssembler.class, GlobalExceptionHandler.class, BookingWebMapperTestConfig.class})
@DisplayName("BookingController")
class BookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookSlotUseCase bookSlotUseCase;

    @MockBean
    private CancelBookingUseCase cancelBookingUseCase;

    @MockBean
    private GetBookingUseCase getBookingUseCase;

    @Nested
    @DisplayName("POST /api/v1/bookings")
    class BookSlotTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should book slot successfully")
        void shouldBookSlotSuccessfully() {
            // Given
            SlotId slotId = SlotId.generate();
            CustomerId customerId = CustomerId.generate();
            BookSlotRequest request = new BookSlotRequest(
                    slotId.toString(),
                    customerId.toString()
            );

            Booking booking = Booking.create(slotId, customerId);

            when(bookSlotUseCase.execute(any(BookSlotCommand.class)))
                    .thenReturn(Mono.just(booking));

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.slotId").isEqualTo(slotId.toString())
                    .jsonPath("$.customerId").isEqualTo(customerId.toString())
                    .jsonPath("$.status").isEqualTo("PENDING");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 404 when slot not found")
        void shouldReturn404WhenSlotNotFound() {
            // Given
            BookSlotRequest request = new BookSlotRequest(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );

            when(bookSlotUseCase.execute(any(BookSlotCommand.class)))
                    .thenReturn(Mono.error(new SlotNotFoundException(request.slotId())));

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SLOT_NOT_FOUND");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 409 when slot not available")
        void shouldReturn409WhenSlotNotAvailable() {
            // Given
            BookSlotRequest request = new BookSlotRequest(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );

            when(bookSlotUseCase.execute(any(BookSlotCommand.class)))
                    .thenReturn(Mono.error(new SlotNotAvailableException("Slot is fully booked")));

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("SLOT_NOT_AVAILABLE");
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() {
            // Given
            BookSlotRequest request = new BookSlotRequest(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookings/{bookingId}")
    class GetBookingTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return booking by ID")
        void shouldReturnBookingById() {
            // Given
            SlotId slotId = SlotId.generate();
            CustomerId customerId = CustomerId.generate();
            Booking booking = Booking.create(slotId, customerId);

            when(getBookingUseCase.findById(booking.getId().toString()))
                    .thenReturn(Mono.just(booking));

            // When/Then
            webTestClient.get()
                    .uri("/api/v1/bookings/{bookingId}", booking.getId().toString())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(booking.getId().toString())
                    .jsonPath("$.slotId").isEqualTo(slotId.toString());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 404 when booking not found")
        void shouldReturn404WhenBookingNotFound() {
            // Given
            String bookingId = UUID.randomUUID().toString();

            when(getBookingUseCase.findById(bookingId))
                    .thenReturn(Mono.error(new BookingNotFoundException(bookingId)));

            // When/Then
            webTestClient.get()
                    .uri("/api/v1/bookings/{bookingId}", bookingId)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("BOOKING_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookings/customer/{customerId}")
    class GetCustomerBookingsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return customer bookings")
        void shouldReturnCustomerBookings() {
            // Given
            SlotId slotId = SlotId.generate();
            CustomerId customerId = CustomerId.generate();
            Booking booking = Booking.create(slotId, customerId);

            when(getBookingUseCase.findByCustomerId(customerId.toString()))
                    .thenReturn(Flux.just(booking));

            // When/Then
            webTestClient.get()
                    .uri("/api/v1/bookings/customer/{customerId}", customerId.toString())
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/bookings/{bookingId}")
    class CancelBookingTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should cancel booking successfully")
        void shouldCancelBookingSuccessfully() {
            // Given
            String bookingId = UUID.randomUUID().toString();

            when(cancelBookingUseCase.execute(any(CancelBookingCommand.class)))
                    .thenReturn(Mono.empty());

            // When/Then
            webTestClient.delete()
                    .uri("/api/v1/bookings/{bookingId}", bookingId)
                    .exchange()
                    .expectStatus().isNoContent();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 404 when booking not found")
        void shouldReturn404WhenBookingNotFound() {
            // Given
            String bookingId = UUID.randomUUID().toString();

            when(cancelBookingUseCase.execute(any(CancelBookingCommand.class)))
                    .thenReturn(Mono.error(new BookingNotFoundException(bookingId)));

            // When/Then
            webTestClient.delete()
                    .uri("/api/v1/bookings/{bookingId}", bookingId)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}
