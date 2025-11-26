package com.crafteam.delivery.domain.service;

import com.crafteam.delivery.domain.model.slot.DeliveryMode;
import com.crafteam.delivery.domain.model.slot.Slot;
import com.crafteam.delivery.domain.model.slot.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Domain service for slot availability operations.
 * Contains logic that doesn't naturally belong to a single aggregate.
 */
public class SlotAvailabilityService {

    /**
     * Finds the best available slot based on criteria.
     * Prefers slots with more remaining capacity and earlier times.
     */
    public Optional<Slot> findBestAvailableSlot(List<Slot> slots, DeliveryMode preferredMode) {
        return slots.stream()
                .filter(Slot::isAvailable)
                .filter(slot -> slot.getDeliveryMode() == preferredMode)
                .max(Comparator.comparingInt(Slot::remainingCapacity)
                        .thenComparing(slot -> slot.getTimeSlot().startTime(), Comparator.reverseOrder()));
    }

    /**
     * Finds available slots within a time range.
     */
    public List<Slot> findSlotsInTimeRange(List<Slot> slots, LocalTime startTime, LocalTime endTime) {
        TimeSlot searchRange = new TimeSlot(startTime, endTime);

        return slots.stream()
                .filter(Slot::isAvailable)
                .filter(slot -> slot.getTimeSlot().overlaps(searchRange))
                .sorted(Comparator.comparing(slot -> slot.getTimeSlot().startTime()))
                .toList();
    }

    /**
     * Checks if a slot transfer is valid.
     */
    public boolean canTransferBooking(Slot sourceSlot, Slot targetSlot) {
        if (!targetSlot.isAvailable()) {
            return false;
        }

        // Same delivery mode required
        if (!sourceSlot.getDeliveryMode().equals(targetSlot.getDeliveryMode())) {
            return false;
        }

        // Cannot transfer to earlier date
        if (targetSlot.getDate().isBefore(sourceSlot.getDate())) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the total available capacity for a given date and mode.
     */
    public int calculateTotalAvailableCapacity(List<Slot> slots, LocalDate date, DeliveryMode mode) {
        return slots.stream()
                .filter(slot -> slot.getDate().equals(date))
                .filter(slot -> slot.getDeliveryMode() == mode)
                .mapToInt(Slot::remainingCapacity)
                .sum();
    }

    /**
     * Finds all slots for a specific date sorted by start time.
     */
    public List<Slot> findSlotsByDate(List<Slot> slots, LocalDate date) {
        return slots.stream()
                .filter(slot -> slot.getDate().equals(date))
                .filter(Slot::isAvailable)
                .sorted(Comparator.comparing(slot -> slot.getTimeSlot().startTime()))
                .toList();
    }
}
