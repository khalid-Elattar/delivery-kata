package com.crafteam.delivery.infrastructure.config;

import com.crafteam.delivery.domain.service.BookingValidator;
import com.crafteam.delivery.domain.service.SlotSuggestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuration for booking-related beans.
 */
@Configuration
public class BookingConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public BookingValidator bookingValidator(
            @Value("${delivery.booking.max-active-per-user:3}") int maxActivePerUser) {
        return new BookingValidator(maxActivePerUser);
    }

    @Bean
    public SlotSuggestionService slotSuggestionService(
            @Value("${delivery.booking.max-active-per-user:3}") int maxActivePerUser) {
        return new SlotSuggestionService(maxActivePerUser);
    }
}
