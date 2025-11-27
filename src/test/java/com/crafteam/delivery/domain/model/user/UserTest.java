package com.crafteam.delivery.domain.model.user;

import com.crafteam.delivery.domain.event.UserDeactivatedEvent;
import com.crafteam.delivery.domain.event.UserRegisteredEvent;
import com.crafteam.delivery.domain.event.UserUpdatedEvent;
import com.crafteam.delivery.domain.model.booking.Booking;
import com.crafteam.delivery.domain.model.slot.SlotId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Aggregate")
class UserTest {

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should register user with valid parameters")
        void shouldRegisterUserWithValidParameters() {
            // Given
            String firstName = "John";
            String lastName = "Doe";
            Email email = Email.from("john.doe@example.com");
            Password password = Password.fromHash("hashedPassword123");
            Address address = Address.of("123 Main St", "Paris", "75001", "France");
            PhoneNumber phoneNumber = PhoneNumber.from("+33612345678");

            // When
            User user = User.register(firstName, lastName, email, password, address, phoneNumber);

            // Then
            assertThat(user.getId()).isNotNull();
            assertThat(user.getFirstName()).isEqualTo(firstName);
            assertThat(user.getLastName()).isEqualTo(lastName);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getAddress()).isEqualTo(address);
            assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should register user without address and phone")
        void shouldRegisterUserWithoutAddressAndPhone() {
            // Given
            Email email = Email.from("jane.doe@example.com");
            Password password = Password.fromHash("hashedPassword456");

            // When
            User user = User.register("Jane", "Doe", email, password, null, null);

            // Then - Address defaults to empty when null is passed
            assertThat(user.getAddress().isEmpty()).isTrue();
            assertThat(user.getPhoneNumber()).isNull();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("should emit UserRegisteredEvent on registration")
        void shouldEmitUserRegisteredEventOnRegistration() {
            // Given
            Email email = Email.from("test@example.com");
            Password password = Password.fromHash("hashedPassword");

            // When
            User user = User.register("Test", "User", email, password, null, null);

            // Then
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegisteredEvent.class);
            UserRegisteredEvent event = (UserRegisteredEvent) user.getDomainEvents().get(0);
            assertThat(event.userId()).isEqualTo(user.getId());
            assertThat(event.email()).isEqualTo(email);
        }

        @Test
        @DisplayName("should throw exception for null first name")
        void shouldThrowExceptionForNullFirstName() {
            // When/Then
            assertThatThrownBy(() -> User.register(
                    null, "Doe", Email.from("test@example.com"), Password.fromHash("hash"), null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("First name is required");
        }

        @Test
        @DisplayName("should throw exception for null last name")
        void shouldThrowExceptionForNullLastName() {
            // When/Then
            assertThatThrownBy(() -> User.register(
                    "John", null, Email.from("test@example.com"), Password.fromHash("hash"), null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Last name is required");
        }

        @Test
        @DisplayName("should throw exception for null email")
        void shouldThrowExceptionForNullEmail() {
            // When/Then
            assertThatThrownBy(() -> User.register(
                    "John", "Doe", null, Password.fromHash("hash"), null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Email is required");
        }

        @Test
        @DisplayName("should throw exception for null password")
        void shouldThrowExceptionForNullPassword() {
            // When/Then
            assertThatThrownBy(() -> User.register(
                    "John", "Doe", Email.from("test@example.com"), null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Password is required");
        }
    }

    @Nested
    @DisplayName("Update Profile")
    class UpdateProfile {

        @Test
        @DisplayName("should update user profile")
        void shouldUpdateUserProfile() {
            // Given
            User user = createTestUser();
            user.clearDomainEvents();
            Address newAddress = Address.of("456 New St", "Lyon", "69001", "France");
            PhoneNumber newPhone = PhoneNumber.from("+33698765432");

            // When
            user.updateProfile("Jane", "Smith", newAddress, newPhone);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getAddress()).isEqualTo(newAddress);
            assertThat(user.getPhoneNumber()).isEqualTo(newPhone);
            assertThat(user.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should emit UserUpdatedEvent on profile update")
        void shouldEmitUserUpdatedEventOnProfileUpdate() {
            // Given
            User user = createTestUser();
            user.clearDomainEvents();

            // When
            user.updateProfile("Jane", "Smith", null, null);

            // Then
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserUpdatedEvent.class);
            UserUpdatedEvent event = (UserUpdatedEvent) user.getDomainEvents().get(0);
            assertThat(event.userId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePassword {

        @Test
        @DisplayName("should change user password")
        void shouldChangeUserPassword() {
            // Given
            User user = createTestUser();
            Password newPassword = Password.fromHash("newHashedPassword");

            // When
            user.changePassword(newPassword);

            // Then
            assertThat(user.getPassword()).isEqualTo(newPassword);
            assertThat(user.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception for null password")
        void shouldThrowExceptionForNullPasswordInChange() {
            // Given
            User user = createTestUser();

            // When/Then
            assertThatThrownBy(() -> user.changePassword(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("New password is required");
        }
    }

    @Nested
    @DisplayName("Deactivate")
    class Deactivate {

        @Test
        @DisplayName("should deactivate user")
        void shouldDeactivateUser() {
            // Given
            User user = createTestUser();
            user.clearDomainEvents();

            // When
            user.deactivate();

            // Then
            assertThat(user.isActive()).isFalse();
            assertThat(user.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should emit UserDeactivatedEvent on deactivation")
        void shouldEmitUserDeactivatedEventOnDeactivation() {
            // Given
            User user = createTestUser();
            user.clearDomainEvents();

            // When
            user.deactivate();

            // Then
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserDeactivatedEvent.class);
            UserDeactivatedEvent event = (UserDeactivatedEvent) user.getDomainEvents().get(0);
            assertThat(event.userId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("should throw exception when deactivating already inactive user")
        void shouldThrowExceptionWhenDeactivatingAlreadyInactiveUser() {
            // Given
            User user = createTestUser();
            user.deactivate();

            // When/Then
            assertThatThrownBy(user::deactivate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("User is already deactivated");
        }
    }

    @Nested
    @DisplayName("Activate")
    class Activate {

        @Test
        @DisplayName("should activate inactive user")
        void shouldActivateInactiveUser() {
            // Given
            User user = createTestUser();
            user.deactivate();
            user.clearDomainEvents();

            // When
            user.activate();

            // Then
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when activating already active user")
        void shouldThrowExceptionWhenActivatingAlreadyActiveUser() {
            // Given
            User user = createTestUser();

            // When/Then
            assertThatThrownBy(user::activate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("User is already active");
        }
    }

    @Nested
    @DisplayName("Full Name")
    class FullName {

        @Test
        @DisplayName("should have first and last name")
        void shouldHaveFirstAndLastName() {
            // Given
            User user = createTestUser();

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    @DisplayName("Bookings Management")
    class BookingsManagement {

        @Test
        @DisplayName("should start with empty bookings list")
        void shouldStartWithEmptyBookingsList() {
            // Given
            User user = createTestUser();

            // Then
            assertThat(user.getBookings()).isEmpty();
            assertThat(user.hasBookings()).isFalse();
            assertThat(user.getBookingsCount()).isZero();
        }

        @Test
        @DisplayName("should add booking to user")
        void shouldAddBookingToUser() {
            // Given
            User user = createTestUser();
            Booking booking = Booking.create(SlotId.generate(), user.getId());

            // When
            user.addBooking(booking);

            // Then
            assertThat(user.getBookings()).hasSize(1);
            assertThat(user.getBookings()).contains(booking);
            assertThat(user.hasBookings()).isTrue();
            assertThat(user.getBookingsCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should add multiple bookings")
        void shouldAddMultipleBookings() {
            // Given
            User user = createTestUser();
            Booking booking1 = Booking.create(SlotId.generate(), user.getId());
            Booking booking2 = Booking.create(SlotId.generate(), user.getId());

            // When
            user.addBookings(List.of(booking1, booking2));

            // Then
            assertThat(user.getBookings()).hasSize(2);
            assertThat(user.getBookingsCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("should not add duplicate booking")
        void shouldNotAddDuplicateBooking() {
            // Given
            User user = createTestUser();
            Booking booking = Booking.create(SlotId.generate(), user.getId());

            // When
            user.addBooking(booking);
            user.addBooking(booking);

            // Then
            assertThat(user.getBookings()).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when adding booking for different user")
        void shouldThrowExceptionWhenAddingBookingForDifferentUser() {
            // Given
            User user = createTestUser();
            UserId differentUserId = UserId.generate();
            Booking booking = Booking.create(SlotId.generate(), differentUserId);

            // When/Then
            assertThatThrownBy(() -> user.addBooking(booking))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Booking does not belong to this user");
        }

        @Test
        @DisplayName("should throw exception when adding null booking")
        void shouldThrowExceptionWhenAddingNullBooking() {
            // Given
            User user = createTestUser();

            // When/Then
            assertThatThrownBy(() -> user.addBooking(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Booking cannot be null");
        }

        @Test
        @DisplayName("should clear bookings")
        void shouldClearBookings() {
            // Given
            User user = createTestUser();
            Booking booking = Booking.create(SlotId.generate(), user.getId());
            user.addBooking(booking);

            // When
            user.clearBookings();

            // Then
            assertThat(user.getBookings()).isEmpty();
            assertThat(user.hasBookings()).isFalse();
        }

        @Test
        @DisplayName("should return unmodifiable bookings list")
        void shouldReturnUnmodifiableBookingsList() {
            // Given
            User user = createTestUser();

            // When/Then
            assertThatThrownBy(() -> user.getBookings().add(Booking.create(SlotId.generate(), user.getId())))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    private User createTestUser() {
        return User.register(
                "John",
                "Doe",
                Email.from("john.doe@example.com"),
                Password.fromHash("hashedPassword123"),
                Address.of("123 Main St", "Paris", "75001", "France"),
                PhoneNumber.from("+33612345678")
        );
    }
}
