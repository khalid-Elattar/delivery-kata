package com.crafteam.delivery.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Slot Booking API")
                        .version("1.0.0")
                        .description("""
                                REST API for Delivery Slot Booking System.

                                ## Features
                                - **Slots Management**: Create and retrieve delivery slots
                                - **Booking Management**: Book, retrieve, and cancel bookings
                                - **Delivery Modes**: DRIVE, DELIVERY, DELIVERY_TODAY, DELIVERY_ASAP

                                ## Authentication
                                Basic authentication is required for all endpoints.
                                - User: `user` / `password` (read access)
                                - Admin: `admin` / `admin` (full access)
                                """)
                        .contact(new Contact()
                                .name("Delivery Team")
                                .email("delivery@crafteam.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
