package com.crafteam.delivery.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * HATEOAS configuration for WebFlux.
 */
@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class HateoasConfig implements WebFluxConfigurer {

    private final ObjectMapper objectMapper;

    public HateoasConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public LinkRelationProvider linkRelationProvider() {
        return new EvoInflectorLinkRelationProvider();
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // Register HAL module for HATEOAS support
        ObjectMapper halMapper = objectMapper.copy();
        halMapper.registerModule(new Jackson2HalModule());

        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(halMapper));
        configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(halMapper));
    }
}
