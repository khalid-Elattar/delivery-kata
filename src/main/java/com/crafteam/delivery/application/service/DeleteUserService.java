package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.in.DeleteUserUseCase;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.exception.UserNotFoundException;
import com.crafteam.delivery.domain.model.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Application service for deleting users.
 */
@Service
@Transactional
public class DeleteUserService implements DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserService.class);

    private final UserRepository userRepository;

    public DeleteUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> execute(String userId) {
        log.info("Deleting user: userId={}", userId);

        UserId id = UserId.from(userId);

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> userRepository.delete(id))
                .doOnSuccess(v -> log.info("User deleted: userId={}", userId));
    }
}
