package com.crafteam.delivery.infrastructure.config;

import com.crafteam.delivery.application.port.out.UserRepository;
import com.crafteam.delivery.domain.model.user.Email;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for WebFlux.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - Auth
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers("/actuator/health").permitAll()

                        // Admin only - Swagger
                        .pathMatchers("/swagger-ui/**", "/api-docs/**", "/webjars/**").hasRole("ADMIN")

                        // Admin only - User management
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/{userId}").hasRole("ADMIN")

                        // User + Admin - Own profile
                        .pathMatchers("/api/v1/users/me/**").hasAnyRole("USER", "ADMIN")

                        // Admin only - Create slots
                        .pathMatchers(HttpMethod.POST, "/api/v1/slots/**").hasRole("ADMIN")

                        // User + Admin - Read slots
                        .pathMatchers(HttpMethod.GET, "/api/v1/slots/**").hasAnyRole("USER", "ADMIN")

                        // User + Admin - Bookings
                        .pathMatchers("/api/v1/bookings/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .httpBasic(httpBasic -> {
                })
                .build();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(Email.from(username))
                .map(user -> User.builder()
                        .username(user.getEmail().value())
                        .password(user.getPassword().hashedValue())
                        .roles(user.getRole().name())
                        .disabled(!user.isActive())
                        .build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
