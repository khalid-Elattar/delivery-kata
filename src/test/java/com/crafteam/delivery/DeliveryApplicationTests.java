package com.crafteam.delivery;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration test requires infrastructure (PostgreSQL, Redis, Kafka)")
class DeliveryApplicationTests {

    @Test
    void contextLoads() {
    }

}
