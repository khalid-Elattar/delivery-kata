package com.crafteam.delivery.infrastructure.adapter.out.persistence.mapper;

import com.crafteam.delivery.domain.model.user.*;
import com.crafteam.delivery.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * MapStruct mapper for converting between User domain objects and UserEntity persistence objects.
 * Bookings are loaded separately via BookingPersistenceMapper (R2DBC best practice).
 */
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "userIdToUuid")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "password", source = "password", qualifiedByName = "passwordToString")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneToString")
    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    @Mapping(target = "isNew", source = "updatedAt", qualifiedByName = "isNewFromUpdatedAt")
    UserEntity toEntity(User user);

    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        Address address = Address.of(
                entity.getStreet(),
                entity.getCity(),
                entity.getZipCode(),
                entity.getCountry()
        );

        PhoneNumber phoneNumber = entity.getPhoneNumber() != null && !entity.getPhoneNumber().isBlank()
                ? PhoneNumber.from(entity.getPhoneNumber())
                : null;

        return User.reconstitute(
                UserId.from(entity.getId()),
                entity.getFirstName(),
                entity.getLastName(),
                Email.from(entity.getEmail()),
                Password.fromHash(entity.getPassword()),
                address,
                phoneNumber,
                UserRole.valueOf(entity.getRole()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getActive()
        );
    }

    @Named("userIdToUuid")
    default UUID userIdToUuid(UserId userId) {
        return userId != null ? userId.value() : null;
    }

    @Named("emailToString")
    default String emailToString(Email email) {
        return email != null ? email.value() : null;
    }

    @Named("passwordToString")
    default String passwordToString(Password password) {
        return password != null ? password.hashedValue() : null;
    }

    @Named("phoneToString")
    default String phoneToString(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.value() : null;
    }

    @Named("roleToString")
    default String roleToString(UserRole role) {
        return role != null ? role.name() : null;
    }

    @Named("isNewFromUpdatedAt")
    default boolean isNewFromUpdatedAt(java.time.Instant updatedAt) {
        // If updatedAt is null, this is a new entity that hasn't been persisted yet
        return updatedAt == null;
    }
}
