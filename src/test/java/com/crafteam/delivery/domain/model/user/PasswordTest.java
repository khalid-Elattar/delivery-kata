package com.crafteam.delivery.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password Value Object")
class PasswordTest {

    @Test
    @DisplayName("should create password from hashed value")
    void shouldCreatePasswordFromHashedValue() {
        // Given
        String hashedValue = "$2a$10$somehashvalue";

        // When
        Password password = Password.fromHash(hashedValue);

        // Then
        assertThat(password.hashedValue()).isEqualTo(hashedValue);
    }

    @Test
    @DisplayName("should throw exception for null hashed value")
    void shouldThrowExceptionForNullHashedValue() {
        // When/Then
        assertThatThrownBy(() -> Password.fromHash(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hashed password value cannot be null");
    }

    @Test
    @DisplayName("should throw exception for blank hashed value")
    void shouldThrowExceptionForBlankHashedValue() {
        // When/Then
        assertThatThrownBy(() -> Password.fromHash("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hashed password cannot be blank");
    }

    @Test
    @DisplayName("should be equal for same hashed value")
    void shouldBeEqualForSameHashedValue() {
        // Given
        Password password1 = Password.fromHash("$2a$10$somehash");
        Password password2 = Password.fromHash("$2a$10$somehash");

        // Then
        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("should not be equal for different hashed values")
    void shouldNotBeEqualForDifferentHashedValues() {
        // Given
        Password password1 = Password.fromHash("$2a$10$hash1");
        Password password2 = Password.fromHash("$2a$10$hash2");

        // Then
        assertThat(password1).isNotEqualTo(password2);
    }
}
