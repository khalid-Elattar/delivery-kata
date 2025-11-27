package com.crafteam.delivery.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("should create email with valid value")
    void shouldCreateEmailWithValidValue() {
        // When
        Email email = Email.from("test@example.com");

        // Then
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@domain.com",
            "user.name@domain.com",
            "user+tag@domain.com",
            "user@subdomain.domain.com"
    })
    @DisplayName("should accept valid email formats")
    void shouldAcceptValidEmailFormats(String validEmail) {
        // When
        Email email = Email.from(validEmail);

        // Then
        assertThat(email.value()).isEqualTo(validEmail);
    }

    @Test
    @DisplayName("should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        // When/Then
        assertThatThrownBy(() -> Email.from(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Email value cannot be null");
    }

    @Test
    @DisplayName("should throw exception for blank email")
    void shouldThrowExceptionForBlankEmail() {
        // When/Then
        assertThatThrownBy(() -> Email.from("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@domain.com",
            "invalid@domain",
            "invalid email@domain.com"
    })
    @DisplayName("should throw exception for invalid email formats")
    void shouldThrowExceptionForInvalidEmailFormats(String invalidEmail) {
        // When/Then
        assertThatThrownBy(() -> Email.from(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("should return value via toString")
    void shouldReturnValueViaToString() {
        // Given
        Email email = Email.from("test@example.com");

        // Then
        assertThat(email.toString()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("should be equal for same email value")
    void shouldBeEqualForSameEmailValue() {
        // Given
        Email email1 = Email.from("test@example.com");
        Email email2 = Email.from("test@example.com");

        // Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }
}
