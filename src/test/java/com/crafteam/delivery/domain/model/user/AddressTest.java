package com.crafteam.delivery.domain.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Value Object")
class AddressTest {

    @Test
    @DisplayName("should create address with all fields")
    void shouldCreateAddressWithAllFields() {
        // When
        Address address = Address.of("123 Main St", "Paris", "75001", "France");

        // Then
        assertThat(address.street()).isEqualTo("123 Main St");
        assertThat(address.city()).isEqualTo("Paris");
        assertThat(address.zipCode()).isEqualTo("75001");
        assertThat(address.country()).isEqualTo("France");
        assertThat(address.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("should allow null fields since address fields are optional")
    void shouldAllowNullFields() {
        // When
        Address address = Address.of(null, null, null, null);

        // Then
        assertThat(address.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should create empty address")
    void shouldCreateEmptyAddress() {
        // When
        Address address = Address.empty();

        // Then
        assertThat(address.isEmpty()).isTrue();
        assertThat(address.toString()).isEmpty();
    }

    @Test
    @DisplayName("should throw exception for blank street when provided")
    void shouldThrowExceptionForBlankStreet() {
        // When/Then
        assertThatThrownBy(() -> Address.of("   ", "Paris", "75001", "France"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Street cannot be blank if provided");
    }

    @Test
    @DisplayName("should throw exception for blank city when provided")
    void shouldThrowExceptionForBlankCity() {
        // When/Then
        assertThatThrownBy(() -> Address.of("123 Main St", "   ", "75001", "France"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("City cannot be blank if provided");
    }

    @Test
    @DisplayName("should throw exception for blank zip code when provided")
    void shouldThrowExceptionForBlankZipCode() {
        // When/Then
        assertThatThrownBy(() -> Address.of("123 Main St", "Paris", "   ", "France"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Zip code cannot be blank if provided");
    }

    @Test
    @DisplayName("should throw exception for blank country when provided")
    void shouldThrowExceptionForBlankCountry() {
        // When/Then
        assertThatThrownBy(() -> Address.of("123 Main St", "Paris", "75001", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Country cannot be blank if provided");
    }

    @Test
    @DisplayName("should be equal for same address values")
    void shouldBeEqualForSameAddressValues() {
        // Given
        Address address1 = Address.of("123 Main St", "Paris", "75001", "France");
        Address address2 = Address.of("123 Main St", "Paris", "75001", "France");

        // Then
        assertThat(address1).isEqualTo(address2);
        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @Test
    @DisplayName("should not be equal for different address values")
    void shouldNotBeEqualForDifferentAddressValues() {
        // Given
        Address address1 = Address.of("123 Main St", "Paris", "75001", "France");
        Address address2 = Address.of("456 Other St", "Lyon", "69001", "France");

        // Then
        assertThat(address1).isNotEqualTo(address2);
    }

    @Test
    @DisplayName("should format toString correctly")
    void shouldFormatToStringCorrectly() {
        // Given
        Address address = Address.of("123 Main St", "Paris", "75001", "France");

        // Then
        assertThat(address.toString()).isEqualTo("123 Main St, Paris 75001, France");
    }
}
