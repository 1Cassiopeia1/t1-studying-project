package ru.t1.java.demo.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class FeignClientConfiguration {
    private final CustomAuthorizationManager customAuthorizationManager;

    @Bean
    RequestInterceptor requestInterceptor() {
        return template -> {
            if (!template.headers().containsKey("Authorization")) {
                var accessToken = customAuthorizationManager.getAccessToken();
                template.header("Authorization", "Bearer %s".formatted(accessToken));
            }
        };
    }
}
