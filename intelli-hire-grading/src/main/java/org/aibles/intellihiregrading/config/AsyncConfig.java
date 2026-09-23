package org.aibles.intellihiregrading.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Getter
@Setter
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "grading.thread-pool")
public class AsyncConfig {

    private int coreSize = 10;
    private int maxSize = 20;
    private int queueCapacity = 100;

    @Bean(name = "gradingTaskExecutor")
    public Executor gradingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("grading-llm-");
        executor.initialize();
        return executor;
    }
}
