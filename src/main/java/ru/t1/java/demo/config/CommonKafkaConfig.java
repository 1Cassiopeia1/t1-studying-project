package ru.t1.java.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class CommonKafkaConfig {
    @Bean
    public RecordMessageConverter recordMessageConverter(ObjectMapper objectMapper) {
        return new StringJsonMessageConverter(objectMapper);
    }
}
