package com.crafteam.delivery.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Output port for password encoding operations.
 * Uses reactive types to avoid blocking the event loop.
 */
public interface PasswordEncoderPort {

    /**
     * Encodes a raw password reactively.
     * The CPU-intensive BCrypt operation is offloaded to a bounded elastic scheduler.
     *
     * @param rawPassword the raw password to encode
     * @return Mono containing the encoded password
     */
    Mono<String> encode(String rawPassword);

    /**
     * Checks if a raw password matches an encoded password reactively.
     * The CPU-intensive BCrypt operation is offloaded to a bounded elastic scheduler.
     *
     * @param rawPassword the raw password
     * @param encodedPassword the encoded password
     * @return Mono containing true if they match, false otherwise
     */
    Mono<Boolean> matches(String rawPassword, String encodedPassword);
}
