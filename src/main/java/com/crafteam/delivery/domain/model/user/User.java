package com.crafteam.delivery.domain.model.user;

import com.crafteam.delivery.domain.event.UserDeactivatedEvent;
import com.crafteam.delivery.domain.event.UserRegisteredEvent;
import com.crafteam.delivery.domain.event.UserUpdatedEvent;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.shared.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root representing a user in the system.
 */
public class User {

    private final UserId id;
    private String firstName;
    private String lastName;
    private final Email email;
    private Password password;
    private Address address;
    private PhoneNumber phoneNumber;
    private final UserRole role;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean active;

    // Bookings associated with this user (loaded on demand)
    private final List<Booking> bookings = new ArrayList<>();

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private User(UserId id, String firstName, String lastName, Email email,
                 Password password, Address address, PhoneNumber phoneNumber,
                 UserRole role, Instant createdAt, Instant updatedAt, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    /**
     * Factory method for registering a new user.
     */
    public static User register(String firstName, String lastName, Email email,
                                Password password, Address address, PhoneNumber phoneNumber) {
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(password, "Password is required");

        if (firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }

        User user = new User(
                UserId.generate(),
                firstName.trim(),
                lastName.trim(),
                email,
                password,
                address != null ? address : Address.empty(),
                phoneNumber,
                UserRole.USER,
                Instant.now(),
                null,
                true
        );

        user.domainEvents.add(new UserRegisteredEvent(
                user.id, user.email, user.firstName, user.lastName, Instant.now()
        ));

        return user;
    }

    /**
     * Factory method for creating an admin user.
     */
    public static User createAdmin(String firstName, String lastName, Email email,
                                   Password password, Address address, PhoneNumber phoneNumber) {
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(password, "Password is required");

        return new User(
                UserId.generate(),
                firstName.trim(),
                lastName.trim(),
                email,
                password,
                address != null ? address : Address.empty(),
                phoneNumber,
                UserRole.ADMIN,
                Instant.now(),
                null,
                true
        );
    }

    /**
     * Factory method for reconstituting a user from persistence.
     */
    public static User reconstitute(UserId id, String firstName, String lastName, Email email,
                                    Password password, Address address, PhoneNumber phoneNumber,
                                    UserRole role, Instant createdAt, Instant updatedAt, boolean active) {
        return new User(id, firstName, lastName, email, password, address, phoneNumber,
                role, createdAt, updatedAt, active);
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Updates the user's profile information.
     */
    public void updateProfile(String firstName, String lastName, Address address, PhoneNumber phoneNumber) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName.trim();
        }
        if (address != null) {
            this.address = address;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        this.updatedAt = Instant.now();

        domainEvents.add(new UserUpdatedEvent(this.id, this.email, Instant.now()));
    }

    /**
     * Changes the user's password.
     */
    public void changePassword(Password newPassword) {
        Objects.requireNonNull(newPassword, "New password is required");
        this.password = newPassword;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.active = false;
        this.updatedAt = Instant.now();

        domainEvents.add(new UserDeactivatedEvent(this.id, this.email, Instant.now()));
    }

    /**
     * Activates the user account.
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("User is already active");
        }
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if this user is an admin.
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    // ========== BOOKING MANAGEMENT ==========

    /**
     * Adds a booking to this user's booking list.
     * This is typically called when loading bookings from persistence.
     */
    public void addBooking(Booking booking) {
        Objects.requireNonNull(booking, "Booking cannot be null");
        if (!booking.getUserId().equals(this.id)) {
            throw new IllegalArgumentException("Booking does not belong to this user");
        }
        if (!bookings.contains(booking)) {
            bookings.add(booking);
        }
    }

    /**
     * Adds multiple bookings to this user's booking list.
     * This is typically called when loading bookings from persistence.
     */
    public void addBookings(List<Booking> bookingsToAdd) {
        Objects.requireNonNull(bookingsToAdd, "Bookings list cannot be null");
        bookingsToAdd.forEach(this::addBooking);
    }

    /**
     * Clears the bookings list.
     * Useful when refreshing bookings from persistence.
     */
    public void clearBookings() {
        bookings.clear();
    }

    /**
     * Returns an unmodifiable view of this user's bookings.
     */
    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    /**
     * Returns the number of bookings for this user.
     */
    public int getBookingsCount() {
        return bookings.size();
    }

    /**
     * Checks if the user has any bookings.
     */
    public boolean hasBookings() {
        return !bookings.isEmpty();
    }

    // ========== EVENT HANDLING ==========

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ========== GETTERS ==========

    public UserId getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Address getAddress() {
        return address;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    // ========== EQUALITY (by ID) ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=%s, email=%s, firstName=%s, lastName=%s, role=%s, active=%s}"
                .formatted(id, email, firstName, lastName, role, active);
    }
}
