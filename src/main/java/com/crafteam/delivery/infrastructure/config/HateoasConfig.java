package com.crafteam.delivery.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * HATEOAS configuration for WebFlux.
 * Spring HATEOAS auto-configures WebFlux support when @EnableHypermediaSupport is present.
 */
@Configuration
@EnableHypermediaSupport(
        type = {EnableHypermediaSupport.HypermediaType.HAL}
)
public class HateoasConfig {
    // Spring HATEOAS will automatically configure:
    // - HypermediaWebFluxConfigurer
    // - Jackson encoders/decoders with HAL support
    // - LinkRelationProvider
    // The key is setting the correct Accept and Content-Type headers
}
