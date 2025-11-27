package com.crafteam.delivery.application.port.out;

import com.crafteam.delivery.application.dto.response.TokenClaims;
import com.crafteam.delivery.domain.model.token.AccessToken;
import com.crafteam.delivery.domain.model.user.User;
import reactor.core.publisher.Mono;

/**
 * Port for JWT token generation and validation.
 */
public interface JwtTokenProvider {

    /**
     * Generates an access token for the given user.
     */
    Mono<AccessToken> generateAccessToken(User user);

    /**
     * Extracts userId from access token.
     */
    Mono<String> extractUserId(String token);

    /**
     * Validates access token.
     */
    Mono<Boolean> validateToken(String token);

    /**
     * Extracts all claims from token.
     */
    Mono<TokenClaims> extractClaims(String token);
}
