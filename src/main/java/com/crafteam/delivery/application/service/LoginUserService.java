package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.LoginCommand;
import com.crafteam.delivery.application.port.in.LoginUserUseCase;
import com.crafteam.delivery.application.port.out.PasswordEncoderPort;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.InvalidCredentialsException;
import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Application service for user login.
 */
@Service
public class LoginUserService implements LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public LoginUserService(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> execute(LoginCommand command) {
        log.info("Login attempt: email={}", command.email());

        Email email = Email.from(command.email());

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new InvalidCredentialsException());
                    }

                    // Verify password reactively
                    return passwordEncoder.matches(command.password(), user.getPassword().hashedValue())
                            .flatMap(matches -> {
                                if (!matches) {
                                    log.warn("Login failed: invalid password for email={}", command.email());
                                    return Mono.error(new InvalidCredentialsException());
                                }

                                log.info("Login successful: userId={}, email={}", user.getId(), user.getEmail());
                                return Mono.just(user);
                            });
                });
    }
}
