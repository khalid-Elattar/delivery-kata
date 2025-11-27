package com.crafteam.delivery.interfaces.rest;

import com.crafteam.delivery.application.port.in.*;
import com.crafteam.delivery.interfaces.rest.dto.request.ChangePasswordRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.UpdateUserRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.UserResponse;
import com.crafteam.delivery.interfaces.rest.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for user management.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserWebMapper userWebMapper;

    public UserController(GetUserUseCase getUserUseCase,
                          GetAllUsersUseCase getAllUsersUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          UserWebMapper userWebMapper) {
        this.getUserUseCase = getUserUseCase;
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.userWebMapper = userWebMapper;
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public Mono<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return getUserUseCase.findByEmail(userDetails.getUsername())
                .map(userWebMapper::toResponse);
    }

    @PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update current user profile", description = "Updates the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public Mono<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        return getUserUseCase.findByEmail(userDetails.getUsername())
                .flatMap(user -> updateUserUseCase.execute(
                        userWebMapper.toCommand(request, user.getId().toString())))
                .map(userWebMapper::toResponse);
    }

    @PutMapping(value = "/me/password", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change password", description = "Changes the authenticated user's password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Invalid old password")
    public Mono<UserResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        return getUserUseCase.findByEmail(userDetails.getUsername())
                .flatMap(user -> updateUserUseCase.changePassword(
                        userWebMapper.toCommand(request, user.getId().toString())))
                .map(userWebMapper::toResponse);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all users (Admin only)", description = "Retrieves all users in the system")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public Flux<UserResponse> getAllUsers() {
        return getAllUsersUseCase.execute()
                .map(userWebMapper::toResponse);
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user by ID (Admin only)", description = "Retrieves a specific user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        return getUserUseCase.findById(userId)
                .map(userWebMapper::toResponse);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user (Admin only)", description = "Deletes a user account")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public Mono<Void> deleteUser(@PathVariable String userId) {
        return deleteUserUseCase.execute(userId);
    }
}
