package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.dto.command.LogoutCommand;
import com.crafteam.delivery.application.dto.response.TokenResponse;
import com.crafteam.delivery.application.port.in.LoginUserUseCase;
import com.crafteam.delivery.application.port.in.LogoutUserUseCase;
import com.crafteam.delivery.application.port.in.RefreshAccessTokenUseCase;
import com.crafteam.delivery.application.port.in.RegisterUserUseCase;
import com.crafteam.delivery.application.port.out.JwtTokenProvider;
import com.crafteam.delivery.application.port.out.RefreshTokenRepository;
import com.crafteam.delivery.domain.model.token.RefreshToken;
import com.crafteam.delivery.domain.model.token.TokenValue;
import com.crafteam.delivery.interfaces.rest.dto.request.LoginRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.RefreshTokenRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.RegisterRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.UserResponse;
import com.crafteam.delivery.interfaces.rest.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * REST controller for authentication operations with JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication API with JWT")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUserUseCase logoutUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserWebMapper userWebMapper;

    @Value("${jwt.refresh-token.expiration-days}")
    private int refreshTokenExpirationDays;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          RefreshAccessTokenUseCase refreshAccessTokenUseCase,
                          LogoutUserUseCase logoutUserUseCase,
                          JwtTokenProvider jwtTokenProvider,
                          RefreshTokenRepository refreshTokenRepository,
                          UserWebMapper userWebMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userWebMapper = userWebMapper;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account"
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    public Mono<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return registerUserUseCase.execute(userWebMapper.toCommand(request))
                .map(userWebMapper::toResponse);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Login user",
            description = "Authenticates a user and returns JWT access and refresh tokens"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return loginUserUseCase.execute(userWebMapper.toCommand(request))
                .flatMap(user -> {
                    // Generate access token
                    return jwtTokenProvider.generateAccessToken(user)
                            .flatMap(accessToken -> {
                                // Generate refresh token
                                String refreshTokenValue = UUID.randomUUID().toString();
                                Instant expiresAt = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

                                RefreshToken refreshToken = RefreshToken.create(
                                        user.getId(),
                                        TokenValue.from(refreshTokenValue),
                                        expiresAt
                                );

                                // Save refresh token
                                return refreshTokenRepository.save(refreshToken)
                                        .map(savedToken -> TokenResponse.of(
                                                accessToken.value(),
                                                savedToken.getToken().value(),
                                                3600, // 1 hour in seconds
                                                user.getId().toString(),
                                                user.getEmail().value(),
                                                user.getRole().name()
                                        ));
                            });
                });
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token"
    )
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public Mono<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshAccessTokenUseCase.execute(userWebMapper.toCommand(request));
    }

    @PostMapping(value = "/logout")
    @Operation(
            summary = "Logout user",
            description = "Revokes all refresh tokens for the authenticated user"
    )
    @ApiResponse(responseCode = "204", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> logout(@AuthenticationPrincipal String userId) {
        return logoutUserUseCase.execute(new LogoutCommand(userId));
    }
}
