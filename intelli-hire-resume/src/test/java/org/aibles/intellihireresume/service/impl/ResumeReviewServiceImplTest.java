package org.aibles.intellihireresume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeReviewResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.ResumeReview;
import org.aibles.intellihireresume.repository.ResumeContactRepository;
import org.aibles.intellihireresume.repository.ResumeEducationRepository;
import org.aibles.intellihireresume.repository.ResumeExperienceRepository;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeReviewRepository;
import org.aibles.intellihireresume.repository.ResumeSkillProfileRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.ai.AiProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeReviewServiceImplTest {

    @Mock
    private ResumeReviewRepository reviewRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeFileRepository fileRepository;

    @Mock
    private ResumeContactRepository contactRepository;

    @Mock
    private ResumeExperienceRepository experienceRepository;

    @Mock
    private ResumeEducationRepository educationRepository;

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ResumeSkillProfileRepository profileRepository;

    @Mock
    private AiProvider aiProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResumeReviewServiceImpl resumeReviewService;

    private String testResumeId;

    private static final String VALID_AI_JSON = """
            {
              "overall_score": 7.5,
              "sections": {
                "contact":      {"score": 8.0, "feedback": "..."},
                "experience":   {"score": 7.0, "feedback": "..."},
                "education":    {"score": 9.0, "feedback": "..."},
                "skills":       {"score": 6.5, "feedback": "..."},
                "presentation": {"score": 7.5, "feedback": "..."}
              },
              "strengths":        ["Strong background"],
              "weaknesses":       ["Missing certifications"],
              "priority_actions": ["Add certifications"]
            }
            """;

    @BeforeEach
    void setUp() {
        resumeReviewService = new ResumeReviewServiceImpl(
                reviewRepository,
                resumeRepository,
                fileRepository,
                contactRepository,
                experienceRepository,
                educationRepository,
                resumeSkillRepository,
                skillRepository,
                profileRepository,
                aiProvider,
                objectMapper
        );
        ReflectionTestUtils.setField(resumeReviewService, "outputLanguage", "Vietnamese");

        testResumeId = "resume-123";
    }

    @Test
    void generate_ShouldSendSystemPromptWithConfiguredLanguage_WhenGeneratingReview() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(new Resume()));
        when(fileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(new ResumeFile()));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(experienceRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(educationRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(resumeSkillRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(aiProvider.complete(anyString(), anyString())).thenReturn(VALID_AI_JSON);
        when(reviewRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(ResumeReview.class))).thenAnswer(inv -> inv.getArgument(0));

        resumeReviewService.generate(testResumeId);

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(aiProvider).complete(systemPromptCaptor.capture(), userPromptCaptor.capture());

        assertThat(systemPromptCaptor.getValue()).contains("Vietnamese");
        assertThat(userPromptCaptor.getValue()).contains("Vietnamese");
    }

    @Test
    void generate_ShouldPreserveResponseJsonKeys_WhenParsingAiResponse() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(new Resume()));
        when(fileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(new ResumeFile()));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(experienceRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(educationRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(resumeSkillRepository.findByResumeId(testResumeId)).thenReturn(List.of());
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(aiProvider.complete(anyString(), anyString())).thenReturn(VALID_AI_JSON);
        when(reviewRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(ResumeReview.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeReviewResponse response = resumeReviewService.generate(testResumeId);

        assertThat(response.getOverallScore()).isEqualByComparingTo(new BigDecimal("7.5"));
        assertThat(response.getSections()).containsKeys("contact", "experience", "education", "skills", "presentation");
        assertThat(response.getStrengths()).containsExactly("Strong background");
        assertThat(response.getWeaknesses()).containsExactly("Missing certifications");
        assertThat(response.getPriorityActions()).containsExactly("Add certifications");
    }
}
