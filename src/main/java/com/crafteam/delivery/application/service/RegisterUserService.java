package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.RegisterUserCommand;
import com.crafteam.delivery.application.port.in.RegisterUserUseCase;
import com.crafteam.delivery.application.port.out.EventPublisher;
import com.crafteam.delivery.application.port.out.PasswordEncoderPort;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.EmailAlreadyExistsException;
import com.crafteam.delivery.domain.model.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for user registration.
 */
@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final EventPublisher eventPublisher;

    public RegisterUserService(UserRepository userRepository,
                               PasswordEncoderPort passwordEncoder,
                               EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<User> execute(RegisterUserCommand command) {
        log.info("Registering new user: email={}", command.email());

        Email email = Email.from(command.email());

        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EmailAlreadyExistsException(command.email()));
                    }

                    // Encode password reactively
                    return passwordEncoder.encode(command.password())
                            .map(Password::fromHash)
                            .flatMap(password -> {
                                // Create address (may be null/empty)
                                Address address = Address.of(
                                        command.street(),
                                        command.city(),
                                        command.zipCode(),
                                        command.country()
                                );

                                // Create phone number (may be null)
                                PhoneNumber phoneNumber = command.phoneNumber() != null && !command.phoneNumber().isBlank()
                                        ? PhoneNumber.from(command.phoneNumber())
                                        : null;

                                // Create user through domain factory method
                                User user = User.register(
                                        command.firstName(),
                                        command.lastName(),
                                        email,
                                        password,
                                        address,
                                        phoneNumber
                                );

                                return userRepository.save(user)
                                        .flatMap(savedUser ->
                                                eventPublisher.publishAll(user.getDomainEvents())
                                                        .doOnSuccess(v -> user.clearDomainEvents())
                                                        .thenReturn(savedUser)
                                        );
                            });
                })
                .doOnSuccess(user -> log.info("User registered: id={}, email={}", user.getId(), user.getEmail()));
    }
}
