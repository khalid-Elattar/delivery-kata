package com.crafteam.delivery.infrastructure.adapter.out.cache;

import com.crafteam.delivery.application.port.out.SlotCachePort;
import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.SlotId;
import com.crafteam.delivery.domain.model.slot.TimeSlot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Redis adapter for slot caching.
 */
@Component
public class RedisSlotCacheAdapter implements SlotCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisSlotCacheAdapter.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "slots:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisSlotCacheAdapter(ReactiveRedisTemplate<String, String> redisTemplate,
                                  ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> cacheSlots(DeliveryMode mode, LocalDate date, Flux<Slot> slots) {
        String key = buildKey(mode, date);
        log.debug("Caching slots for key: {}", key);

        return slots.collectList()
                .flatMap(slotList -> {
                    if (slotList.isEmpty()) {
                        return Mono.empty();
                    }
                    return Flux.fromIterable(slotList)
                            .flatMap(slot -> {
                                try {
                                    String json = serializeSlot(slot);
                                    return redisTemplate.opsForList().rightPush(key, json);
                                } catch (JsonProcessingException e) {
                                    log.error("Failed to serialize slot: {}", slot.getId(), e);
                                    return Mono.empty();
                                }
                            })
                            .then(redisTemplate.expire(key, CACHE_TTL))
                            .then();
                });
    }

    @Override
    public Flux<Slot> getCachedSlots(DeliveryMode mode, LocalDate date) {
        String key = buildKey(mode, date);
        log.debug("Getting cached slots for key: {}", key);

        return redisTemplate.opsForList().range(key, 0, -1)
                .flatMap(json -> {
                    try {
                        return Mono.just(deserializeSlot(json));
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize slot from cache", e);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Void> invalidateCache(DeliveryMode mode, LocalDate date) {
        String key = buildKey(mode, date);
        log.debug("Invalidating cache for key: {}", key);

        return redisTemplate.delete(key).then();
    }

    @Override
    public Mono<Void> invalidateAll() {
        log.info("Invalidating all slot cache");
        return redisTemplate.keys(KEY_PREFIX + "*")
                .flatMap(redisTemplate::delete)
                .then();
    }

    private String buildKey(DeliveryMode mode, LocalDate date) {
        return KEY_PREFIX + mode.name() + ":" + date;
    }

    // TODO: DISABLED - Needs rewrite for new Slot architecture
    // The new Slot is a template (no specific date), not a bookable instance
    // Cache strategy needs redesign:
    // Option 1: Cache slot templates separately from booking counts
    // Option 2: Cache computed availability (slot template + date + current bookings)
    // Option 3: Move caching to a higher level that deals with composed data
    private String serializeSlot(Slot slot) throws JsonProcessingException {
        // Temporary serialization for slot templates (without date/bookings)
        Map<String, Object> map = Map.of(
                "id", slot.getId().value().toString(),
                "deliveryMode", slot.getDeliveryMode().name(),
                "startTime", slot.getStartTime().toString(),
                "endTime", slot.getEndTime().toString(),
                "slotDuration", slot.getSlotDuration().toMinutes(),
                "capacity", slot.getCapacity(),
                "availableDays", slot.getAvailableDays().stream()
                        .map(Enum::name)
                        .toList()
        );
        return objectMapper.writeValueAsString(map);
    }

    @SuppressWarnings("unchecked")
    private Slot deserializeSlot(String json) throws JsonProcessingException {
        // Temporary deserialization for slot templates
        Map<String, Object> map = objectMapper.readValue(json, Map.class);

        List<String> dayNames = (List<String>) map.get("availableDays");
        Set<java.time.DayOfWeek> availableDays = dayNames.stream()
                .map(java.time.DayOfWeek::valueOf)
                .collect(java.util.stream.Collectors.toSet());

        return Slot.reconstitute(
                SlotId.from(UUID.fromString((String) map.get("id"))),
                DeliveryMode.valueOf((String) map.get("deliveryMode")),
                availableDays,
                LocalTime.parse((String) map.get("startTime")),
                LocalTime.parse((String) map.get("endTime")),
                java.time.Duration.ofMinutes(((Number) map.get("slotDuration")).longValue()),
                (Integer) map.get("capacity")
        );
    }
}
