package com.crafteam.delivery.application.port.out;

/**
 * Output port for password encoding operations.
 */
public interface PasswordEncoderPort {

    /**
     * Encodes a raw password.
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    String encode(String rawPassword);

    /**
     * Checks if a raw password matches an encoded password.
     * @param rawPassword the raw password
     * @param encodedPassword the encoded password
     * @return true if they match, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
}
