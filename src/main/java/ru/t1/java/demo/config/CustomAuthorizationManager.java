package ru.t1.java.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomAuthorizationManager {

    private final RestClient http;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;

    public CustomAuthorizationManager(RestClient.Builder http,
                                      @Value("${spring.security.oauth2.client.registration.external.client-id}") String clientId,
                                      @Value("${spring.security.oauth2.client.registration.external.client-secret}") String clientSecret,
                                      @Value("${spring.security.oauth2.client.provider.external.token-uri}") String tokenUri) {
        this.http = http.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
    }

    public String getAccessToken() {
        return this.http.post()
                .uri(tokenUri)
                .body(new LogPass(clientId, clientSecret))
                .retrieve()
                .body(Token.class).token();
    }

    private record LogPass(String username, String password) {}
    private record Token(String token) {}
}