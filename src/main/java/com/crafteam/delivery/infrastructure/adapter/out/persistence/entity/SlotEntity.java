package com.crafteam.delivery.infrastructure.adapter.out.persistence.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * R2DBC entity for slots table.
 * Represents a slot availability template (not a specific booking).
 *
 * Implements Persistable to control new vs existing entity detection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("slots")
public class SlotEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @NotBlank(message = "Delivery mode is required")
    @Column("delivery_mode")
    private String deliveryMode;

    /**
     * Comma-separated list of available days (e.g., "MONDAY,TUESDAY,WEDNESDAY").
     */
    @NotBlank(message = "Available days are required")
    @Column("available_days")
    private String availableDays;

    @NotNull(message = "Start time is required")
    @Column("start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column("end_time")
    private LocalTime endTime;

    /**
     * Slot duration in minutes.
     */
    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    @Column("slot_duration")
    private Integer slotDuration;

    /**
     * Maximum number of bookings allowed per time slot per day.
     */
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column("capacity")
    private Integer capacity;

    @Column("created_at")
    private Instant createdAt;

    /**
     * Flag to indicate if this is a new entity (for R2DBC to decide INSERT vs UPDATE).
     */
    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Mark entity as persisted (not new).
     */
    public SlotEntity markAsPersisted() {
        this.isNew = false;
        return this;
    }
}
