package com.crafteam.delivery.infrastructure.config;

import com.crafteam.delivery.infrastructure.security.JwtAuthenticationEntryPoint;
import com.crafteam.delivery.infrastructure.security.JwtAuthenticationWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for WebFlux with JWT authentication.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationWebFilter = jwtAuthenticationWebFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - Auth
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/actuator/health").permitAll()

                        // Public - Swagger/OpenAPI documentation
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/webjars/**").permitAll()

                        // Admin only - User management
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/{userId}").hasRole("ADMIN")

                        // User + Admin - Own profile and logout
                        .pathMatchers("/api/v1/users/me", "/api/v1/users/me/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/logout").hasAnyRole("USER", "ADMIN")

                        // Admin only - Create slots
                        .pathMatchers(HttpMethod.POST, "/api/v1/slots/**").hasRole("ADMIN")

                        // User + Admin - Read slots
                        .pathMatchers(HttpMethod.GET, "/api/v1/slots/**").hasAnyRole("USER", "ADMIN")

                        // User + Admin - Bookings
                        .pathMatchers("/api/v1/bookings/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                // Add JWT filter before authorization
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
