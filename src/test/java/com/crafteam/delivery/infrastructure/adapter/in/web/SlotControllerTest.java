package com.crafteam.delivery.infrastructure.adapter.in.web;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.application.port.in.CreateSlotUseCase;
import com.crafteam.delivery.application.port.in.GetAvailableSlotsUseCase;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import com.crafteam.delivery.infrastructure.adapter.in.web.dto.request.CreateSlotRequest;
import com.crafteam.delivery.infrastructure.adapter.in.web.hateoas.SlotModelAssembler;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(SlotController.class)
@Import({SecurityConfig.class, SlotModelAssembler.class})
@DisplayName("SlotController")
class SlotControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CreateSlotUseCase createSlotUseCase;

    @MockBean
    private GetAvailableSlotsUseCase getAvailableSlotsUseCase;

    @Nested
    @DisplayName("POST /api/v1/slots")
    class CreateSlotTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should create slot successfully as admin")
        void shouldCreateSlotSuccessfullyAsAdmin() {
            // Given
            LocalDate validDate = getNextValidDate(DeliveryMode.DRIVE);
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    validDate,
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    5
            );

            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    validDate,
                    new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                    5
            );

            when(createSlotUseCase.execute(any(CreateSlotCommand.class)))
                    .thenReturn(Mono.just(slot));

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/slots")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.deliveryMode").isEqualTo("DRIVE")
                    .jsonPath("$.capacity").isEqualTo(5)
                    .jsonPath("$.bookedCount").isEqualTo(0)
                    .jsonPath("$.available").isEqualTo(true);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 403 for non-admin user")
        void shouldReturn403ForNonAdminUser() {
            // Given
            LocalDate validDate = getNextValidDate(DeliveryMode.DRIVE);
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    validDate,
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    5
            );

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/slots")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() {
            // Given
            LocalDate validDate = getNextValidDate(DeliveryMode.DRIVE);
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    validDate,
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    5
            );

            // When/Then
            webTestClient.post()
                    .uri("/api/v1/slots")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/slots")
    class GetAvailableSlotsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return available slots")
        void shouldReturnAvailableSlots() {
            // Given
            LocalDate validDate = getNextValidDate(DeliveryMode.DRIVE);
            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    validDate,
                    new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                    5
            );

            when(getAvailableSlotsUseCase.execute(DeliveryMode.DRIVE, validDate))
                    .thenReturn(Flux.just(slot));

            // When/Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/slots")
                            .queryParam("mode", "DRIVE")
                            .queryParam("date", validDate.toString())
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$._embedded").exists();
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() {
            // Given
            LocalDate validDate = LocalDate.now();

            // When/Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/slots")
                            .queryParam("mode", "DRIVE")
                            .queryParam("date", validDate.toString())
                            .build())
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/slots/all")
    class GetAllSlotsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return all slots")
        void shouldReturnAllSlots() {
            // Given
            LocalDate validDate = getNextValidDate(DeliveryMode.DRIVE);
            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    validDate,
                    new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                    5
            );

            when(getAvailableSlotsUseCase.executeAll())
                    .thenReturn(Flux.just(slot));

            // When/Then
            webTestClient.get()
                    .uri("/api/v1/slots/all")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    private LocalDate getNextValidDate(DeliveryMode mode) {
        LocalDate date = LocalDate.now();
        while (!mode.isAvailableFor(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
