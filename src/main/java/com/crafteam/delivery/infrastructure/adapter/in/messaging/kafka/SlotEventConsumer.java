package com.crafteam.delivery.infrastructure.adapter.in.messaging.kafka;

import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.application.port.out.SlotRepository;
import com.crafteam.delivery.domain.event.SlotBookedEvent;
import com.crafteam.delivery.domain.event.SlotCreatedEvent;
import com.crafteam.delivery.domain.model.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Kafka consumer for slot domain events.
 * Listens to the delivery.slots topic and processes slot-related events.
 *
 * Implemented business logic:
 * - Cache warming for new slots
 * - Cache invalidation on slot changes
 * - Low capacity alerts
 * - Analytics and metrics logging
 */
@Component
public class SlotEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SlotEventConsumer.class);
    private static final double LOW_CAPACITY_THRESHOLD = 0.2; // 20%

    private final SlotCachePort slotCachePort;
    private final SlotRepository slotRepository;

    public SlotEventConsumer(SlotCachePort slotCachePort, SlotRepository slotRepository) {
        this.slotCachePort = slotCachePort;
        this.slotRepository = slotRepository;
    }

    /**
     * Handles SlotCreatedEvent.
     * This could trigger:
     * - Cache warming for new slots
     * - Search index update
     * - Analytics tracking
     * - External calendar synchronization
     */
    @KafkaListener(
            topics = "delivery.slots",
            groupId = "slot-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "slotCreatedFilter"
    )
    public void handleSlotCreated(
            @Payload SlotCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received SlotCreatedEvent: slotId={}, mode={}, date={}, timeSlot={}, capacity={} [topic={}, partition={}, offset={}]",
                event.slotId(),
                event.deliveryMode(),
                event.date(),
                event.timeSlot(),
                event.capacity(),
                topic,
                partition,
                offset);

        try {
            // 1. Warm up cache with the new slot
            slotRepository.findById(event.slotId())
                    .flatMap(slot -> {
                        // Cache this individual slot by fetching all slots for the same mode/date and re-caching
                        return slotRepository.findAvailableByDeliveryModeAndDate(slot.getDeliveryMode(), slot.getDate())
                                .collectList()
                                .flatMap(slots -> slotCachePort.cacheSlots(
                                        slot.getDeliveryMode(),
                                        slot.getDate(),
                                        Flux.fromIterable(slots)
                                ));
                    })
                    .doOnSuccess(v -> log.debug("Cache warmed for new slot: {}", event.slotId()))
                    .doOnError(e -> log.warn("Failed to warm cache for slot: {}", event.slotId(), e))
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                    .block(); // Block is acceptable in Kafka listener

            // 2. Log analytics event
            log.info("ANALYTICS: Slot created - slotId={}, mode={}, date={}, capacity={}, timeSlot={}",
                    event.slotId(), event.deliveryMode(), event.date(), event.capacity(), event.timeSlot());

            // 3. TODO: Update search index (ElasticSearch)
            // searchService.indexSlot(event.slotId());

            // 4. TODO: Send notification to admin dashboard
            // adminDashboardService.notifyNewSlot(event);

            // 5. TODO: Sync with external calendar/scheduling system
            // externalCalendarService.createEvent(event);

            log.info("Successfully processed SlotCreatedEvent: slotId={}", event.slotId());

        } catch (Exception e) {
            log.error("Error processing SlotCreatedEvent: slotId={}", event.slotId(), e);
            throw e;
        }
    }

    /**
     * Handles SlotBookedEvent.
     * This could trigger:
     * - Real-time availability update
     * - Cache invalidation
     * - Capacity threshold alerts
     * - Analytics update
     */
    @KafkaListener(
            topics = "delivery.slots",
            groupId = "slot-service",
            containerFactory = "kafkaListenerContainerFactory",
            filter = "slotBookedFilter"
    )
    public void handleSlotBooked(
            @Payload SlotBookedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received SlotBookedEvent: slotId={}, userId={}, remainingCapacity={} [topic={}, partition={}, offset={}]",
                event.slotId(),
                event.userId(),
                event.remainingCapacity(),
                topic,
                partition,
                offset);

        try {
            // 1. Invalidate cache to reflect updated availability
            slotRepository.findById(event.slotId())
                    .flatMap(slot -> slotCachePort.invalidateCache(slot.getDeliveryMode(), slot.getDate()))
                    .doOnSuccess(v -> log.debug("Cache invalidated for booked slot: {}", event.slotId()))
                    .doOnError(e -> log.warn("Failed to invalidate cache for slot: {}", event.slotId(), e))
                    .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                    .block(); // Block is acceptable in Kafka listener

            // 2. Check capacity thresholds and send alerts
            slotRepository.findById(event.slotId())
                    .subscribe(slot -> {
                        double capacityUsage = (double) slot.getBookedCount() / slot.getCapacity();

                        if (event.remainingCapacity() == 0) {
                            log.warn("ALERT: Slot FULL - slotId={}, mode={}, date={}, timeSlot={}",
                                    event.slotId(), slot.getDeliveryMode(), slot.getDate(), slot.getTimeSlot());
                            // TODO: Send alert to operations team
                            // alertService.sendSlotFullAlert(event.slotId());

                        } else if (capacityUsage >= (1 - LOW_CAPACITY_THRESHOLD)) {
                            log.warn("ALERT: Slot LOW CAPACITY - slotId={}, remaining={}/{}, usage={}%",
                                    event.slotId(), event.remainingCapacity(), slot.getCapacity(),
                                    String.format("%.0f", capacityUsage * 100));
                            // TODO: Send low capacity warning
                            // alertService.sendLowCapacityWarning(event.slotId(), event.remainingCapacity());
                        }
                    });

            // 3. Log analytics event
            log.info("ANALYTICS: Slot booked - slotId={}, userId={}, remainingCapacity={}",
                    event.slotId(), event.userId(), event.remainingCapacity());

            // 4. TODO: Update real-time dashboard
            // dashboardService.updateSlotMetrics(event.slotId(), event.remainingCapacity());

            // 5. TODO: Trigger recommendation engine update
            // recommendationService.updateAvailability(event.deliveryMode(), event.date());

            // 6. TODO: Notify delivery coordinators if threshold reached
            // if (event.remainingCapacity() == 0) {
            //     deliveryCoordinatorService.notifySlotFull(event.slotId());
            // }

            log.info("Successfully processed SlotBookedEvent: slotId={}", event.slotId());

        } catch (Exception e) {
            log.error("Error processing SlotBookedEvent: slotId={}", event.slotId(), e);
            throw e;
        }
    }
}
