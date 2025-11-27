package com.crafteam.delivery.application.service;

import com.crafteam.delivery.application.dto.command.LogoutCommand;
import com.crafteam.delivery.application.port.in.LogoutUserUseCase;
import com.crafteam.delivery.application.port.out.RefreshTokenRepository;
import com.crafteam.delivery.domain.model.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for user logout (revoke all refresh tokens).
 */
@Service
public class LogoutUserService implements LogoutUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutUserService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUserService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public Mono<Void> execute(LogoutCommand command) {
        UserId userId = UserId.from(command.userId());

        log.info("Logging out user: userId={}", userId);

        return refreshTokenRepository.deleteByUserId(userId)
                .doOnSuccess(v -> log.info("Successfully logged out user: userId={}", userId))
                .doOnError(e -> log.error("Error logging out user: userId={}", userId, e));
    }
}
