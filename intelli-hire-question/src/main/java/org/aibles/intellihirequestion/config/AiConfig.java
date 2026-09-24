package org.aibles.intellihirequestion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihirequestion.service.ai.QuestionGenerationAiClient;
import org.aibles.intellihirequestion.service.ai.impl.OpenRouterQuestionGenerationClient;
import org.aibles.intellihirequestion.service.client.ResumeAggregateClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiProperties.class, ResumeClientProperties.class})
public class AiConfig {

    @Bean
    public QuestionGenerationAiClient questionGenerationAiClient(
            AiProperties aiProperties,
            ObjectMapper objectMapper,
            @Value("${ai.output-language:Vietnamese}") String outputLanguage) {
        return new OpenRouterQuestionGenerationClient(aiProperties.getOpenrouter(), objectMapper, outputLanguage);
    }

    @Bean
    public ResumeAggregateClient resumeAggregateClient(ResumeClientProperties properties) {
        return new ResumeAggregateClient(properties);
    }
}
