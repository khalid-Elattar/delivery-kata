package com.crafteam.delivery.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * R2DBC entity for bookings table.
 */
@Table("bookings")
public class BookingEntity {

    @Id
    private UUID id;

    @Column("slot_id")
    private UUID slotId;

    @Column("customer_id")
    private UUID customerId;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;

    @Column("confirmed_at")
    private Instant confirmedAt;

    @Column("cancelled_at")
    private Instant cancelledAt;

    public BookingEntity() {
    }

    public BookingEntity(UUID id, UUID slotId, UUID customerId, String status,
                         Instant createdAt, Instant confirmedAt, Instant cancelledAt) {
        this.id = id;
        this.slotId = slotId;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
