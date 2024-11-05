package ru.t1.java.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация Сваггера.
 */
@Configuration
@ConditionalOnProperty("springdoc.swagger-ui.enabled")
@RequiredArgsConstructor
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI(ServletContext servletContext) {
        var server = new Server().url(servletContext.getContextPath());

        var contact = new Contact()
                .name("Маркова Ксения Евгеньевна");

        var info = new Info()
                .title(applicationName)
                .description("Тестовый сервис")
                .version("0.0.1")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
