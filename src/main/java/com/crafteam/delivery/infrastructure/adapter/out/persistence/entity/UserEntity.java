package com.crafteam.delivery.infrastructure.adapter.out.persistence.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import java.util.UUID;

/**
 * R2DBC entity for users table.
 * Implements Persistable to control new vs existing entity detection.
 * Bookings are loaded separately via BookingRepository (R2DBC best practice).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Column("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Column("last_name")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column("email")
    private String email;

    @NotBlank(message = "Password is required")
    @Column("password")
    private String password;

    @Size(max = 255, message = "Street must not exceed 255 characters")
    @Column("street")
    private String street;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column("city")
    private String city;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    @Column("zip_code")
    private String zipCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column("country")
    private String country;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Role is required")
    @Column("role")
    private String role;

    @NotNull(message = "Active status is required")
    @Column("active")
    private Boolean active;

    @NotNull(message = "Created at is required")
    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

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
    public UserEntity markAsPersisted() {
        this.isNew = false;
        return this;
    }
}
