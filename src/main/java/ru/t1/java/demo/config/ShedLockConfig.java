package ru.t1.java.demo.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "5m")
public class ShedLockConfig {

    @Value("${redis.url}")
    private String url;

    @Bean
    public JedisClient jedisClient() {
        return new JedisClient(url);
    }

    @Bean
    public LockProvider lockProvider(JedisClient jedisClient) {
        return new JedisLockProvider(jedisClient);
    }
}
