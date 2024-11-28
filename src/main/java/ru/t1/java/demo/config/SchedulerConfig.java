package ru.t1.java.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {
    @Bean
    public TaskScheduler clientTaskExecutor() {
        var executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(1);
        executor.setThreadNamePrefix("client-unlock-task-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskScheduler accountTaskExecutor() {
        var executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(1);
        executor.setThreadNamePrefix("account-unlock-task-");
        executor.initialize();
        return executor;
    }
}
