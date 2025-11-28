package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.CreateSlotCommand;
import com.crafteam.delivery.application.port.in.CreateSlotUseCase;
import com.crafteam.delivery.application.port.in.GetAvailableSlotsUseCase;
import com.crafteam.delivery.application.port.in.GetSlotAvailabilityUseCase;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.interfaces.rest.dto.request.CreateSlotRequest;
import com.crafteam.delivery.interfaces.rest.hateoas.SlotModelAssembler;
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
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(SlotController.class)
@Import({SecurityConfig.class, SlotModelAssembler.class, SlotWebMapperTestConfig.class})
@DisplayName("SlotController")
class SlotControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CreateSlotUseCase createSlotUseCase;

    @MockBean
    private GetAvailableSlotsUseCase getAvailableSlotsUseCase;

    @MockBean
    private GetSlotAvailabilityUseCase getSlotAvailabilityUseCase;

    @MockBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("POST /api/v1/slots")
    class CreateSlotTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should create slot template successfully as admin")
        void shouldCreateSlotSuccessfullyAsAdmin() {
            // Given - Create slot template request
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    60, // 60 minutes duration
                    10
            );

            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10
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
                    .jsonPath("$.capacity").isEqualTo(10);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 403 for non-admin user")
        void shouldReturn403ForNonAdminUser() {
            // Given - Create slot template request
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    List.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    60,
                    10
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
            CreateSlotRequest request = new CreateSlotRequest(
                    DeliveryMode.DRIVE,
                    List.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    60,
                    10
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
        @DisplayName("should return available slot templates")
        void shouldReturnAvailableSlots() {
            // Given
            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10
            );

            when(getAvailableSlotsUseCase.execute(any(DeliveryMode.class), any()))
                    .thenReturn(Flux.just(slot));

            // When/Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/slots")
                            .queryParam("mode", "DRIVE")
                            .queryParam("date", "2024-01-15")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() {
            // When/Then
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/slots")
                            .queryParam("mode", "DRIVE")
                            .queryParam("date", "2024-01-15")
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
        @DisplayName("should return all slot templates")
        void shouldReturnAllSlots() {
            // Given
            Slot slot = Slot.create(
                    DeliveryMode.DRIVE,
                    Set.of(DayOfWeek.MONDAY),
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    Duration.ofMinutes(60),
                    10
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
}
