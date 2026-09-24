package org.aibles.intellihiregrading.utils;

import org.aibles.intellihiregrading.dto.AnswerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void build_withAllFields_containsAllValues() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("What is Spring Boot?")
                .answerText("A framework for Java")
                .skillName("spring-core")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java Backend", "Vietnamese");

        assertTrue(prompt.contains("Java Backend"));
        assertTrue(prompt.contains("spring-core"));
        assertTrue(prompt.contains("What is Spring Boot?"));
        assertTrue(prompt.contains("A framework for Java"));
        assertTrue(prompt.contains("10.0"));
        assertTrue(prompt.contains("JSON"));
        assertTrue(prompt.contains("Vietnamese"));
    }

    @Test
    void build_withNullTopic_usesGeneral() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText("A")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, null, "Vietnamese");

        assertTrue(prompt.contains("Topic: General"));
    }

    @Test
    void build_withNullSkillName_usesGeneral() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText("A")
                .skillName(null)
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", "Vietnamese");

        assertTrue(prompt.contains("Skill: General"));
    }

    @Test
    void build_withNullAnswer_usesNoAnswerProvided() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText(null)
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", "Vietnamese");

        assertTrue(prompt.contains("(no answer provided)"));
    }

    @Test
    void build_withNullQuestionText_usesEmpty() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText(null)
                .answerText("A")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", "Vietnamese");

        assertTrue(prompt.contains("Question: \n"));
    }

    @Test
    void build_promptContainsJsonInstruction() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText("A")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", "Vietnamese");

        assertTrue(prompt.contains("\"score\""));
        assertTrue(prompt.contains("\"feedback\""));
    }

    @Test
    void build_withConfiguredLanguage_usesGivenLanguage() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText("A")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", "English");

        assertTrue(prompt.contains("English"));
        assertFalse(prompt.contains("Vietnamese"));
    }

    @Test
    void build_withNullLanguage_defaultsToVietnamese() {
        AnswerRequest answer = AnswerRequest.builder()
                .questionId("q1")
                .questionText("Q?")
                .answerText("A")
                .maxScore(10.0)
                .build();

        String prompt = promptBuilder.build(answer, "Java", null);

        assertTrue(prompt.contains("Vietnamese"));
    }
}
