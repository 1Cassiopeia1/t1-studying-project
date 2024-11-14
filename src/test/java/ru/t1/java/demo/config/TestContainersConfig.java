package ru.t1.java.demo.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(LiquibaseConfig.class)
@ActiveProfiles("test")
public interface TestContainersConfig {

    @Container
    @ServiceConnection
    PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withReuse(true);
}
