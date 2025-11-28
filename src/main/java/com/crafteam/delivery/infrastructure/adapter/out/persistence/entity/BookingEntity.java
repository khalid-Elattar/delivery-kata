package com.crafteam.delivery.infrastructure.adapter.out.persistence.entity;

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
 * R2DBC entity for bookings table.
 * Implements Persistable to control new vs existing entity detection.
 * Represents the many-to-one relationship with User (via user_id).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bookings")
public class BookingEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @NotNull(message = "Slot ID is required")
    @Column("slot_id")
    private UUID slotId;

    @NotNull(message = "User ID is required")
    @Column("user_id")
    private UUID userId;

    @Column("booking_date")
    private LocalDate bookingDate;

    @Column("booking_time")
    private LocalTime bookingTime;

    @NotBlank(message = "Status is required")
    @Column("status")
    private String status;

    @NotNull(message = "Created at is required")
    @Column("created_at")
    private Instant createdAt;

    @Column("confirmed_at")
    private Instant confirmedAt;

    @Column("cancelled_at")
    private Instant cancelledAt;

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
    public BookingEntity markAsPersisted() {
        this.isNew = false;
        return this;
    }
}
