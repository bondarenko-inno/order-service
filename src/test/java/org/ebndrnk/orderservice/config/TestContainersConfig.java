package org.ebndrnk.orderservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Configuration class for Testcontainers PostgreSQL and Redis containers.
 * Starts and manages lifecycle of test database and Redis containers,
 * and registers their connection properties for Spring tests.
 */
public class TestContainersConfig {

    /**
     * PostgreSQL test container instance.
     */
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15.4")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");



    static {
        postgres.start();
    }

    /**
     * Registers dynamic datasource and Redis properties from the running containers
     * into the Spring application context.
     *
     * @param registry the dynamic property registry to add properties to
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
