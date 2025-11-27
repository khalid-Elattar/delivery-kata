package com.crafteam.delivery.domain.model.user;

/**
 * Value Object representing a physical address.
 */
public record Address(
        String street,
        String city,
        String zipCode,
        String country
) {

    public Address {
        // All fields are optional but if provided, they should not be blank
        if (street != null && street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be blank if provided");
        }
        if (city != null && city.isBlank()) {
            throw new IllegalArgumentException("City cannot be blank if provided");
        }
        if (zipCode != null && zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code cannot be blank if provided");
        }
        if (country != null && country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be blank if provided");
        }
    }

    public static Address of(String street, String city, String zipCode, String country) {
        return new Address(street, city, zipCode, country);
    }

    public static Address empty() {
        return new Address(null, null, null, null);
    }

    public boolean isEmpty() {
        return street == null && city == null && zipCode == null && country == null;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (street != null) sb.append(street);
        if (city != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(city);
        }
        if (zipCode != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(zipCode);
        }
        if (country != null) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }
}
