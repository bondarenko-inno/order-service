package org.ebndrnk.orderservice;

import org.ebndrnk.orderservice.config.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceApplicationTests extends TestContainersConfig {

    @Test
    void contextLoads() {
    }

}
