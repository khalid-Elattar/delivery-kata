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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * R2DBC entity for slots table.
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

    @NotNull(message = "Date is required")
    @Column("date")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    @Column("start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column("end_time")
    private LocalTime endTime;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Column("capacity")
    private int capacity;

    @Min(value = 0, message = "Booked count cannot be negative")
    @Column("booked_count")
    private int bookedCount;

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
