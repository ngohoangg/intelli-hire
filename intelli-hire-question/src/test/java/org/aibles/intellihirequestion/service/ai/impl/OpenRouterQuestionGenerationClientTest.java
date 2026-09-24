package org.aibles.intellihirequestion.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihirequestion.config.AiProperties;
import org.aibles.intellihirequestion.dto.GenerateQuestionsFromResumeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenRouterQuestionGenerationClient")
class OpenRouterQuestionGenerationClientTest {

    private AiProperties.OpenRouterConfig config;

    @BeforeEach
    void setUp() {
        config = new AiProperties.OpenRouterConfig();
        config.setApiKey("test-key");
        config.setTimeoutSeconds(5);
    }

    private OpenRouterQuestionGenerationClient clientWithConfiguredLanguage(String configuredLanguage) {
        return new OpenRouterQuestionGenerationClient(config, new ObjectMapper(), configuredLanguage);
    }

    private GenerateQuestionsFromResumeRequest requestWithLanguage(String language) {
        GenerateQuestionsFromResumeRequest request = new GenerateQuestionsFromResumeRequest();
        request.setResumeId("resume-1");
        request.setCount(3);
        request.setLanguage(language);
        return request;
    }

    @Nested
    @DisplayName("resolveOutputLanguage()")
    class ResolveOutputLanguageTests {

        @Test
        @DisplayName("Should fall back to configured language when request language is blank")
        void shouldFallBackToConfiguredLanguage_whenRequestLanguageIsBlank() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");

            assertThat(client.resolveOutputLanguage(null)).isEqualTo("Vietnamese");
            assertThat(client.resolveOutputLanguage("")).isEqualTo("Vietnamese");
            assertThat(client.resolveOutputLanguage("   ")).isEqualTo("Vietnamese");
        }

        @Test
        @DisplayName("Should default to Vietnamese when no language is configured or requested")
        void shouldDefaultToVietnamese_whenNothingConfiguredOrRequested() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage(null);

            assertThat(client.resolveOutputLanguage(null)).isEqualTo("Vietnamese");
            assertThat(client.resolveOutputLanguage("")).isEqualTo("Vietnamese");
        }

        @Test
        @DisplayName("Should prefer the request language over the configured default")
        void shouldPreferRequestLanguage_overConfiguredDefault() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");

            assertThat(client.resolveOutputLanguage("French")).isEqualTo("French");
        }

        @Test
        @DisplayName("Should normalize known short codes to their full language name")
        void shouldNormalizeKnownShortCodes() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");

            assertThat(client.resolveOutputLanguage("vi")).isEqualTo("Vietnamese");
            assertThat(client.resolveOutputLanguage("vi-VN")).isEqualTo("Vietnamese");
            assertThat(client.resolveOutputLanguage("en")).isEqualTo("English");
            assertThat(client.resolveOutputLanguage("en-US")).isEqualTo("English");
        }

        @Test
        @DisplayName("Should pass through any other non-blank value as given")
        void shouldPassThroughOtherValues() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");

            assertThat(client.resolveOutputLanguage("Japanese")).isEqualTo("Japanese");
            assertThat(client.resolveOutputLanguage("ja-JP")).isEqualTo("ja-JP");
        }

        @Test
        @DisplayName("Should normalize a configured short code default too")
        void shouldNormalizeConfiguredShortCodeDefault() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("vi");

            assertThat(client.resolveOutputLanguage(null)).isEqualTo("Vietnamese");
        }
    }

    @Nested
    @DisplayName("buildSystemPrompt()")
    class BuildSystemPromptTests {

        @Test
        @DisplayName("Should include the configured default language when request language is blank")
        void shouldIncludeConfiguredLanguage_whenRequestLanguageIsBlank() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");
            GenerateQuestionsFromResumeRequest request = requestWithLanguage(null);

            String prompt = client.buildSystemPrompt(request, "", "");

            assertThat(prompt).contains("Write all human-readable text values (candidateSummary, questionText, rationale) in Vietnamese.");
        }

        @Test
        @DisplayName("Should include the request-provided language when present")
        void shouldIncludeRequestLanguage_whenPresent() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");
            GenerateQuestionsFromResumeRequest request = requestWithLanguage("en");

            String prompt = client.buildSystemPrompt(request, "", "");

            assertThat(prompt).contains("Write all human-readable text values (candidateSummary, questionText, rationale) in English.");
        }

        @Test
        @DisplayName("Should keep JSON keys and catalog-matched fields instruction untranslated")
        void shouldKeepJsonKeysInstructionUntranslated() {
            OpenRouterQuestionGenerationClient client = clientWithConfiguredLanguage("Vietnamese");
            GenerateQuestionsFromResumeRequest request = requestWithLanguage(null);

            String prompt = client.buildSystemPrompt(request, "", "");

            assertThat(prompt).contains("Keep JSON keys, enum values (questionType), and catalog-matched names (topicName, difficultyName) exactly as specified in English/catalog form");
        }
    }
}
