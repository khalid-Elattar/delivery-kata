package com.crafteam.delivery.infrastructure.adapter.in.messaging.kafka;

import com.crafteam.delivery.domain.event.UserDeactivatedEvent;
import com.crafteam.delivery.domain.event.UserRegisteredEvent;
import com.crafteam.delivery.domain.event.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for user domain events.
 * Listens to the delivery.events topic and processes user-related events.
 *
 * Note: User events are published to the default "delivery.events" topic
 * as defined in KafkaEventPublisher.
 */
@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    /**
     * Handles UserRegisteredEvent.
     * This could trigger:
     * - Welcome email
     * - Account setup workflow
     * - Analytics tracking
     * - CRM synchronization
     * - Marketing automation
     */
    @KafkaListener(
            topics = "delivery.events",
            groupId = "user-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "userRegisteredFilter"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received UserRegisteredEvent: userId={}, email={}, name={} {} [topic={}, partition={}, offset={}]",
                event.userId(),
                event.email(),
                event.firstName(),
                event.lastName(),
                topic,
                partition,
                offset);

        try {
            // 1. Log analytics event
            log.info("ANALYTICS: User registered - userId={}, email={}, name={} {}",
                    event.userId(), event.email(), event.firstName(), event.lastName());

            // 2. TODO: Send welcome email
            // emailService.sendWelcomeEmail(event.email(), event.firstName());
            log.debug("TODO: Send welcome email to {}", event.email());

            // 3. TODO: Send SMS verification
            // smsService.sendVerificationCode(event.phoneNumber());

            // 4. TODO: Create user profile in CRM system
            // crmService.createUser(event.userId(), event.email(), event.firstName(), event.lastName());
            log.debug("TODO: Sync user {} to CRM", event.userId());

            // 5. TODO: Trigger onboarding workflow
            // onboardingService.startOnboarding(event.userId());

            // 6. TODO: Create loyalty program account
            // loyaltyService.createAccount(event.userId());

            // 7. TODO: Send to marketing automation platform
            // marketingService.addContact(event.email(), event.firstName(), event.lastName());

            log.info("Successfully processed UserRegisteredEvent: userId={}", event.userId());

        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent: userId={}", event.userId(), e);
            throw e;
        }
    }

    /**
     * Handles UserUpdatedEvent.
     * This could trigger:
     * - Profile synchronization
     * - Cache invalidation
     * - Analytics update
     * - CRM update
     */
    @KafkaListener(
            topics = "delivery.events",
            groupId = "user-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "userUpdatedFilter"
    )
    public void handleUserUpdated(
            @Payload UserUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received UserUpdatedEvent: userId={} [topic={}, partition={}, offset={}]",
                event.userId(),
                topic,
                partition,
                offset);

        try {
            // 1. Log analytics event
            log.info("ANALYTICS: User updated - userId={}", event.userId());

            // 2. TODO: Invalidate user cache if implemented
            // userCachePort.invalidate(event.userId());
            log.debug("TODO: Invalidate user cache for userId={}", event.userId());

            // 3. TODO: Sync with external systems (CRM, analytics)
            // crmService.updateUser(event.userId());
            // analyticsService.trackUserUpdate(event.userId());

            // 4. TODO: Update search index if user search is implemented
            // searchService.updateUserIndex(event.userId());

            // 5. TODO: Trigger address validation workflow if address changed
            // if (event.hasAddressChanged()) {
            //     addressValidationService.validate(event.userId(), event.newAddress());
            // }

            log.info("Successfully processed UserUpdatedEvent: userId={}", event.userId());

        } catch (Exception e) {
            log.error("Error processing UserUpdatedEvent: userId={}", event.userId(), e);
            throw e;
        }
    }

    /**
     * Handles UserDeactivatedEvent.
     * This could trigger:
     * - Account cleanup
     * - Cancellation of active bookings
     * - Analytics update
     * - CRM deactivation
     * - Data retention workflow
     */
    @KafkaListener(
            topics = "delivery.events",
            groupId = "user-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "userDeactivatedFilter"
    )
    public void handleUserDeactivated(
            @Payload UserDeactivatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received UserDeactivatedEvent: userId={}, email={} [topic={}, partition={}, offset={}]",
                event.userId(),
                event.email(),
                topic,
                partition,
                offset);

        try {
            log.warn("User deactivated: userId={}, email={}", event.userId(), event.email());

            // 1. Log analytics event
            log.info("ANALYTICS: User deactivated - userId={}, email={}", event.userId(), event.email());

            // 2. TODO: Cancel all active bookings for this user
            // bookingRepository.findByUserIdAndStatus(event.userId(), BookingStatus.CONFIRMED)
            //     .flatMap(booking -> cancelBookingUseCase.execute(new CancelBookingCommand(booking.getId())))
            //     .subscribe();
            log.warn("TODO: Cancel all active bookings for deactivated user: {}", event.userId());

            // 3. TODO: Clean up user sessions
            // sessionService.invalidateAllSessions(event.userId());

            // 4. TODO: Deactivate in CRM
            // crmService.deactivateUser(event.userId());
            log.debug("TODO: Deactivate user {} in CRM", event.userId());

            // 5. TODO: Archive user data (GDPR compliance)
            // dataArchiveService.archiveUserData(event.userId());
            log.debug("TODO: Archive data for user {} (GDPR)", event.userId());

            // 6. TODO: Send account closure confirmation email
            // emailService.sendAccountClosureEmail(event.email());
            log.debug("TODO: Send account closure email to {}", event.email());

            // 7. TODO: Trigger data retention policy
            // dataRetentionService.scheduleUserDataDeletion(event.userId(), 30); // 30 days retention

            log.info("Successfully processed UserDeactivatedEvent: userId={}", event.userId());

        } catch (Exception e) {
            log.error("Error processing UserDeactivatedEvent: userId={}", event.userId(), e);
            throw e;
        }
    }
}
