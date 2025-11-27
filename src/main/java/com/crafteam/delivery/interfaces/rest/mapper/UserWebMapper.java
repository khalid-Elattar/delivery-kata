package com.crafteam.delivery.interfaces.rest.mapper;

import com.crafteam.delivery.application.dto.command.ChangePasswordCommand;
import com.crafteam.delivery.application.dto.command.LoginCommand;
import com.crafteam.delivery.application.dto.command.RefreshTokenCommand;
import com.crafteam.delivery.application.dto.command.RegisterUserCommand;
import com.crafteam.delivery.application.dto.command.UpdateUserCommand;
import com.crafteam.delivery.domain.model.user.*;
import com.crafteam.delivery.interfaces.rest.dto.request.ChangePasswordRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.LoginRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.RefreshTokenRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.RegisterRequest;
import com.crafteam.delivery.interfaces.rest.dto.request.UpdateUserRequest;
import com.crafteam.delivery.interfaces.rest.dto.response.LoginResponse;
import com.crafteam.delivery.interfaces.rest.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for web layer user conversions.
 */
@Mapper(componentModel = "spring")
public interface UserWebMapper {

    RegisterUserCommand toCommand(RegisterRequest request);

    LoginCommand toCommand(LoginRequest request);

    RefreshTokenCommand toCommand(RefreshTokenRequest request);

    @Mapping(target = "userId", source = "userId")
    UpdateUserCommand toCommand(UpdateUserRequest request, String userId);

    @Mapping(target = "userId", source = "userId")
    ChangePasswordCommand toCommand(ChangePasswordRequest request, String userId);

    @Mapping(target = "id", source = "id", qualifiedByName = "userIdToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneToString")
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    UserResponse toResponse(User user);

    default LoginResponse toLoginResponse(User user) {
        return new LoginResponse(
                user.getId().toString(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                "Login successful"
        );
    }

    @Named("userIdToString")
    default String userIdToString(UserId userId) {
        return userId != null ? userId.toString() : null;
    }

    @Named("emailToString")
    default String emailToString(Email email) {
        return email != null ? email.value() : null;
    }

    @Named("phoneToString")
    default String phoneToString(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.value() : null;
    }

    @Named("roleToString")
    default String roleToString(UserRole role) {
        return role != null ? role.name() : null;
    }
}
