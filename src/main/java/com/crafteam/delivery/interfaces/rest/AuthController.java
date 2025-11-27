package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.port.in.LoginUserUseCase;
import com.crafteam.delivery.application.port.in.RegisterUserUseCase;
import com.crafteam.delivery.interfaces.rest.dto.request.LoginRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.RegisterRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.LoginResponse;
import com.crafteam.delivery.interfaces.rest.dto.response.UserResponse;
import com.crafteam.delivery.interfaces.rest.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication API")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UserWebMapper userWebMapper;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          UserWebMapper userWebMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
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
            description = "Authenticates a user with email and password"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return loginUserUseCase.execute(userWebMapper.toCommand(request))
                .map(userWebMapper::toLoginResponse);
    }
}
