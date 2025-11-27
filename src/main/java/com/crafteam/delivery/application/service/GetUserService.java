package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.in.GetUserUseCase;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.UserNotFoundException;
import com.crafteam.delivery.domain.model.user.Email;
import com.crafteam.delivery.domain.model.user.User;
import com.crafteam.delivery.domain.model.user.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Application service for retrieving user information.
 */
@Service
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> findById(String userId) {
        return userRepository.findById(UserId.from(userId))
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(Email.from(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException(email)));
    }
}
