package com.crafteam.delivery.infrastructure.adapter.in.messaging.kafka;

import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.event.BookingCancelledEvent;
import com.crafteam.delivery.domain.event.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for booking domain events.
 * Listens to the delivery.bookings topic and processes booking-related events.
 *
 * Implemented business logic:
 * - Cache invalidation for slot availability
 * - Low capacity alerts
 * - Analytics and metrics logging
 * - Hooks for future email/SMS notifications
 */
@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);
    private static final double LOW_CAPACITY_THRESHOLD = 0.2; // 20%

    private final SlotCachePort slotCachePort;
    private final SlotRepository slotRepository;

    public BookingEventConsumer(SlotCachePort slotCachePort, SlotRepository slotRepository) {
        this.slotCachePort = slotCachePort;
        this.slotRepository = slotRepository;
    }

    /**
     * Handles BookingConfirmedEvent.
     * This could trigger:
     * - Email notification to user
     * - SMS confirmation
     * - Analytics update
     * - External system synchronization
     */
    @KafkaListener(
            topics = "delivery.bookings",
            groupId = "booking-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "bookingConfirmedFilter"
    )
    public void handleBookingConfirmed(
            @Payload BookingConfirmedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received BookingConfirmedEvent: bookingId={}, slotId={}, userId={} [topic={}, partition={}, offset={}]",
                event.bookingId(),
                event.slotId(),
                event.userId(),
                topic,
                partition,
                offset);

        try {
            // 1. Invalidate slot cache to reflect updated availability
            slotRepository.findById(event.slotId())
                    .flatMap(slot -> slotCachePort.invalidateCache(slot.getDeliveryMode(), slot.getDate()))
                    .doOnSuccess(v -> log.debug("Cache invalidated for slot: {}", event.slotId()))
                    .doOnError(e -> log.warn("Failed to invalidate cache for slot: {}", event.slotId(), e))
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty()) // Don't fail event processing if cache fails
                    .block(); // Block is acceptable in Kafka listener (separate thread pool)

            // 2. Log analytics event
            log.info("ANALYTICS: Booking confirmed - bookingId={}, slotId={}, userId={}",
                    event.bookingId(), event.slotId(), event.userId());

            // 3. TODO: Send confirmation email/SMS (when email service is implemented)
            // emailService.sendBookingConfirmation(event.userId(), event.bookingId());
            log.debug("TODO: Send confirmation email to userId={}", event.userId());

            // 4. TODO: Sync with external delivery management system
            // deliveryManagementClient.createDelivery(event);

            // 5. TODO: Update real-time dashboard
            // dashboardService.updateBookingMetrics(event.deliveryMode());

            log.info("Successfully processed BookingConfirmedEvent: bookingId={}", event.bookingId());

        } catch (Exception e) {
            log.error("Error processing BookingConfirmedEvent: bookingId={}", event.bookingId(), e);
            // In production, consider:
            // - Dead letter queue (DLQ) for failed messages
            // - Retry mechanism with exponential backoff
            // - Alert/monitoring integration
            throw e; // Let Kafka retry mechanism handle it
        }
    }

    /**
     * Handles BookingCancelledEvent.
     * This could trigger:
     * - Cancellation email to user
     * - Slot capacity update notification
     * - Refund processing
     * - Analytics update
     */
    @KafkaListener(
            topics = "delivery.bookings",
            groupId = "booking-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "bookingCancelledFilter"
    )
    public void handleBookingCancelled(
            @Payload BookingCancelledEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received BookingCancelledEvent: bookingId={}, slotId={}, releasedCapacity={} [topic={}, partition={}, offset={}]",
                event.bookingId(),
                event.slotId(),
                event.releasedCapacity(),
                topic,
                partition,
                offset);

        try {
            // 1. Invalidate slot cache to reflect restored availability
            slotRepository.findById(event.slotId())
                    .flatMap(slot -> slotCachePort.invalidateCache(slot.getDeliveryMode(), slot.getDate()))
                    .doOnSuccess(v -> log.debug("Cache invalidated after cancellation for slot: {}", event.slotId()))
                    .doOnError(e -> log.warn("Failed to invalidate cache for slot: {}", event.slotId(), e))
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                    .block(); // Block is acceptable in Kafka listener

            // 2. Log analytics event
            log.info("ANALYTICS: Booking cancelled - bookingId={}, slotId={}, releasedCapacity={}",
                    event.bookingId(), event.slotId(), event.releasedCapacity());

            // 3. Check if slot availability was restored (capacity now available)
            if (event.releasedCapacity() > 0) {
                log.info("Slot capacity restored: slotId={}, releasedCapacity={}",
                        event.slotId(), event.releasedCapacity());
                // TODO: Notify waiting list if implemented
                // waitingListService.notifyAvailability(event.slotId());
            }

            // 4. TODO: Send cancellation confirmation email
            // emailService.sendCancellationConfirmation(event.bookingId());
            log.debug("TODO: Send cancellation email for bookingId={}", event.bookingId());

            // 5. TODO: Process refund if payment was made
            // refundService.processRefund(event.bookingId());

            // 6. TODO: Notify delivery partners to cancel delivery
            // deliveryManagementClient.cancelDelivery(event.bookingId());

            log.info("Successfully processed BookingCancelledEvent: bookingId={}", event.bookingId());

        } catch (Exception e) {
            log.error("Error processing BookingCancelledEvent: bookingId={}", event.bookingId(), e);
            throw e;
        }
    }
}
