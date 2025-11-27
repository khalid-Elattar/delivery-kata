package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.ChangePasswordCommand;
import com.crafteam.delivery.application.dto.command.UpdateUserCommand;
import com.crafteam.delivery.application.port.in.UpdateUserUseCase;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.PasswordEncoderPort;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.InvalidCredentialsException;
import com.crafteam.delivery.domain.exception.UserNotFoundException;
import com.crafteam.delivery.domain.model.user.Address;
import com.crafteam.delivery.domain.model.user.Password;
import com.crafteam.delivery.domain.model.user.PhoneNumber;
import com.crafteam.delivery.domain.model.user.User;
import com.crafteam.delivery.domain.model.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for updating user information.
 */
@Service
@Transactional
public class UpdateUserService implements UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final EventPublisher eventPublisher;

    public UpdateUserService(UserRepository userRepository,
                             PasswordEncoderPort passwordEncoder,
                             EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<User> execute(UpdateUserCommand command) {
        log.info("Updating user: userId={}", command.userId());

        return userRepository.findById(UserId.from(command.userId()))
                .switchIfEmpty(Mono.error(new UserNotFoundException(command.userId())))
                .flatMap(user -> {
                    Address address = Address.of(
                            command.street(),
                            command.city(),
                            command.zipCode(),
                            command.country()
                    );

                    PhoneNumber phoneNumber = command.phoneNumber() != null && !command.phoneNumber().isBlank()
                            ? PhoneNumber.from(command.phoneNumber())
                            : null;

                    user.updateProfile(
                            command.firstName(),
                            command.lastName(),
                            address,
                            phoneNumber
                    );

                    return userRepository.save(user)
                            .flatMap(savedUser ->
                                    eventPublisher.publishAll(user.getDomainEvents())
                                            .doOnSuccess(v -> user.clearDomainEvents())
                                            .thenReturn(savedUser)
                            );
                })
                .doOnSuccess(user -> log.info("User updated: id={}", user.getId()));
    }

    @Override
    public Mono<User> changePassword(ChangePasswordCommand command) {
        log.info("Changing password: userId={}", command.userId());

        return userRepository.findById(UserId.from(command.userId()))
                .switchIfEmpty(Mono.error(new UserNotFoundException(command.userId())))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(command.oldPassword(), user.getPassword().hashedValue())) {
                        log.warn("Password change failed: invalid old password for userId={}", command.userId());
                        return Mono.error(new InvalidCredentialsException());
                    }

                    String hashedPassword = passwordEncoder.encode(command.newPassword());
                    user.changePassword(Password.fromHash(hashedPassword));

                    return userRepository.save(user);
                })
                .doOnSuccess(user -> log.info("Password changed: userId={}", user.getId()));
    }
}
