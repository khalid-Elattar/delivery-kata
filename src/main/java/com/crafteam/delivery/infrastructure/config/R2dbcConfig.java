package com.crafteam.delivery.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * R2DBC configuration for reactive database access.
 * Schema management is handled by Liquibase (see db/changelog/).
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.crafteam.delivery.infrastructure.adapter.out.persistence.repository")
@EnableR2dbcAuditing
public class R2dbcConfig {
}
