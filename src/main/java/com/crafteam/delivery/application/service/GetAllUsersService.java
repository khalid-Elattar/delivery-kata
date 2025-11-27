package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.port.in.GetAllUsersUseCase;
import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.model.user.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Application service for retrieving all users (admin only).
 */
@Service
public class GetAllUsersService implements GetAllUsersUseCase {

    private final UserRepository userRepository;

    public GetAllUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Flux<User> execute() {
        return userRepository.findAll();
    }
}
