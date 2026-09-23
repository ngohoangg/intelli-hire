package org.aibles.intellihirequestion.service.impl;

import org.aibles.intellihirequestion.dto.StudyArticleRequest;
import org.aibles.intellihirequestion.dto.StudyArticleResponse;
import org.aibles.intellihirequestion.dto.PageResponse;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.StudyArticle;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.InactiveResourceException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.StudyArticleMapper;
import org.aibles.intellihirequestion.projection.StudyArticleDetailProjection;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.StudyArticleRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyArticleServiceImpl")
class StudyArticleServiceImplTest {

    @Mock
    private StudyArticleRepository studyArticleRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private DifficultyLevelRepository difficultyLevelRepository;

    private final StudyArticleMapper studyArticleMapper = new StudyArticleMapper();

    private StudyArticleServiceImpl studyArticleService;

    @BeforeEach
    void setUp() {
        studyArticleService = new StudyArticleServiceImpl(studyArticleRepository, topicRepository, difficultyLevelRepository, studyArticleMapper);
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Should return active study articles with pagination when no filters are provided")
        void shouldReturnActiveStudyArticles_whenNoFiltersProvided() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            StudyArticle article1 = createStudyArticle(
                    "article-1",
                    "topic-1",
                    "difficulty-1",
                    "Java Collections Guide",
                    "https://example.com/java-collections",
                    "Collections deep dive",
                    "Baeldung",
                    true
            );
            StudyArticle article2 = createStudyArticle(
                    "article-2",
                    "topic-2",
                    "difficulty-2",
                    "Spring Transactions",
                    "https://example.com/spring-transactions",
                    "Transaction propagation",
                    "Spring Docs",
                    true
            );

            when(studyArticleRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(article1, article2), pageable, 2));
            when(topicRepository.findAllById(eq(java.util.Set.of("topic-1", "topic-2"))))
                    .thenReturn(List.of(
                            createTopic("topic-1", "Java Core", true),
                            createTopic("topic-2", "Spring", true)
                    ));
            when(difficultyLevelRepository.findAllById(eq(java.util.Set.of("difficulty-1", "difficulty-2"))))
                    .thenReturn(List.of(
                            createDifficultyLevel("difficulty-1", "Intermediate"),
                            createDifficultyLevel("difficulty-2", "Advanced")
                    ));

            PageResponse<StudyArticleResponse> result = studyArticleService.getAll(null, null, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getContent().get(0).getTopicName()).isEqualTo("Java Core");
            assertThat(result.getContent().get(0).getDifficultyName()).isEqualTo("Intermediate");
            assertThat(result.getContent().get(1).getTopicName()).isEqualTo("Spring");
            assertThat(result.getContent().get(1).getDifficultyName()).isEqualTo("Advanced");

            verify(studyArticleRepository).findAll(any(Specification.class), eq(pageable));
            verify(topicRepository).findAllById(eq(java.util.Set.of("topic-1", "topic-2")));
            verify(difficultyLevelRepository).findAllById(eq(java.util.Set.of("difficulty-1", "difficulty-2")));
        }

        @Test
        @DisplayName("Should return active study articles filtered by topicId")
        void shouldReturnActiveStudyArticles_whenFilteredByTopicId() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            StudyArticle article = createStudyArticle(
                    "article-1",
                    "topic-1",
                    "difficulty-1",
                    "Java Collections Guide",
                    "https://example.com/java-collections",
                    "Collections deep dive",
                    "Baeldung",
                    true
            );

            when(studyArticleRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(article), pageable, 1));
            when(topicRepository.findAllById(eq(java.util.Set.of("topic-1"))))
                    .thenReturn(List.of(createTopic("topic-1", "Java Core", true)));
            when(difficultyLevelRepository.findAllById(eq(java.util.Set.of("difficulty-1"))))
                    .thenReturn(List.of(createDifficultyLevel("difficulty-1", "Intermediate")));

            PageResponse<StudyArticleResponse> result = studyArticleService.getAll("topic-1", null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTopicId()).isEqualTo("topic-1");
            assertThat(result.getContent().get(0).getTopicName()).isEqualTo("Java Core");
            assertThat(result.getContent().get(0).getDifficultyName()).isEqualTo("Intermediate");

            verify(studyArticleRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return active study articles filtered by difficultyId")
        void shouldReturnActiveStudyArticles_whenFilteredByDifficultyId() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            StudyArticle article = createStudyArticle(
                    "article-1",
                    "topic-1",
                    "difficulty-1",
                    "Java Collections Guide",
                    "https://example.com/java-collections",
                    "Collections deep dive",
                    "Baeldung",
                    true
            );

            when(studyArticleRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(article), pageable, 1));
            when(topicRepository.findAllById(eq(java.util.Set.of("topic-1"))))
                    .thenReturn(List.of(createTopic("topic-1", "Java Core", true)));
            when(difficultyLevelRepository.findAllById(eq(java.util.Set.of("difficulty-1"))))
                    .thenReturn(List.of(createDifficultyLevel("difficulty-1", "Intermediate")));

            PageResponse<StudyArticleResponse> result = studyArticleService.getAll(null, "difficulty-1", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDifficultyId()).isEqualTo("difficulty-1");
            assertThat(result.getContent().get(0).getTopicName()).isEqualTo("Java Core");
            assertThat(result.getContent().get(0).getDifficultyName()).isEqualTo("Intermediate");

            verify(studyArticleRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return study article details when id exists")
        void shouldReturnStudyArticleDetails_whenIdExists() {
            StudyArticleDetailProjection projection = createProjection(
                    "article-1",
                    "topic-1",
                    "Java Core",
                    "difficulty-1",
                    "Intermediate",
                    "Java Collections Guide",
                    "https://example.com/java-collections",
                    "A practical guide to Java Collections",
                    "Baeldung",
                    true
            );

            when(studyArticleRepository.findByIdProjection("article-1")).thenReturn(Optional.of(projection));

            StudyArticleResponse result = studyArticleService.getById("article-1");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("article-1");
            assertThat(result.getTopicId()).isEqualTo("topic-1");
            assertThat(result.getTopicName()).isEqualTo("Java Core");
            assertThat(result.getDifficultyId()).isEqualTo("difficulty-1");
            assertThat(result.getDifficultyName()).isEqualTo("Intermediate");
            assertThat(result.getTitle()).isEqualTo("Java Collections Guide");
            assertThat(result.getUrl()).isEqualTo("https://example.com/java-collections");
            assertThat(result.getDescription()).isEqualTo("A practical guide to Java Collections");
            assertThat(result.getSource()).isEqualTo("Baeldung");
            assertThat(result.getIsActive()).isTrue();

            verify(studyArticleRepository).findByIdProjection("article-1");
        }

        @Test
        @DisplayName("Should return study article even when topic or difficulty names are missing")
        void shouldReturnStudyArticle_whenJoinedNamesAreMissing() {
            StudyArticleDetailProjection projection = createProjection(
                    "article-2",
                    "topic-2",
                    null,
                    "difficulty-2",
                    null,
                    "Spring Transaction Basics",
                    "https://example.com/spring-transaction",
                    "Transaction propagation explained",
                    "Internal Wiki",
                    true
            );

            when(studyArticleRepository.findByIdProjection("article-2")).thenReturn(Optional.of(projection));

            StudyArticleResponse result = studyArticleService.getById("article-2");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("article-2");
            assertThat(result.getTopicName()).isNull();
            assertThat(result.getDifficultyName()).isNull();
            assertThat(result.getIsActive()).isTrue();

            verify(studyArticleRepository).findByIdProjection("article-2");
        }

        @Test
        @DisplayName("Should throw NotFoundException with SA_001 when study article is inactive")
        void shouldThrowNotFoundException_whenStudyArticleIsInactive() {
            StudyArticleDetailProjection projection = createProjection(
                    "article-3",
                    "topic-1",
                    "Java Core",
                    "difficulty-1",
                    "Intermediate",
                    "Inactive article",
                    "https://example.com/inactive",
                    "Should not be returned",
                    "Internal Wiki",
                    false
            );

            when(studyArticleRepository.findByIdProjection("article-3")).thenReturn(Optional.of(projection));

            assertThatThrownBy(() -> studyArticleService.getById("article-3"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.SA_001.getMessage());

            verify(studyArticleRepository).findByIdProjection("article-3");
        }

        @Test
        @DisplayName("Should throw NotFoundException with SA_001 when id does not exist")
        void shouldThrowNotFoundException_whenIdDoesNotExist() {
            when(studyArticleRepository.findByIdProjection("missing-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.getById("missing-id"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.SA_001.getMessage());

            verify(studyArticleRepository).findByIdProjection("missing-id");
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Should create study article with active status when request omits isActive")
        void shouldCreateStudyArticleWithDefaultActiveStatus_whenRequestOmitsIsActive() {
            StudyArticleRequest request = createRequest(
                    "topic-1",
                    "difficulty-1",
                    "System Design Roadmap",
                    "https://example.com/system-design",
                    null,
                    null,
                    null
            );
            Topic topic = createTopic("topic-1", "Backend", true);
            DifficultyLevel difficultyLevel = createDifficultyLevel("difficulty-1", "Senior");

            when(topicRepository.findById("topic-1")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("difficulty-1")).thenReturn(Optional.of(difficultyLevel));
            when(studyArticleRepository.save(any(StudyArticle.class))).thenAnswer(invocation -> {
                StudyArticle studyArticle = invocation.getArgument(0);
                studyArticle.setId("article-1");
                return studyArticle;
            });

            StudyArticleResponse result = studyArticleService.create(request);

            ArgumentCaptor<StudyArticle> articleCaptor = ArgumentCaptor.forClass(StudyArticle.class);
            verify(studyArticleRepository).save(articleCaptor.capture());
            verify(studyArticleRepository, never()).findByIdProjection(anyString());

            StudyArticle savedArticle = articleCaptor.getValue();
            assertThat(savedArticle.getTopicId()).isEqualTo("topic-1");
            assertThat(savedArticle.getDifficultyId()).isEqualTo("difficulty-1");
            assertThat(savedArticle.getTitle()).isEqualTo("System Design Roadmap");
            assertThat(savedArticle.getUrl()).isEqualTo("https://example.com/system-design");
            assertThat(savedArticle.getDescription()).isNull();
            assertThat(savedArticle.getSource()).isNull();
            assertThat(savedArticle.getIsActive()).isTrue();

            assertThat(result.getId()).isEqualTo("article-1");
            assertThat(result.getTopicName()).isEqualTo("Backend");
            assertThat(result.getDifficultyName()).isEqualTo("Senior");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getSource()).isNull();
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should preserve explicit inactive status when request sets isActive false")
        void shouldPreserveExplicitInactiveStatus_whenRequestSetsIsActiveFalse() {
            StudyArticleRequest request = createRequest(
                    "topic-1",
                    "difficulty-1",
                    "Spring Boot Interview Checklist",
                    "https://example.com/spring-checklist",
                    "A focused checklist for interview preparation",
                    "Engineering Blog",
                    false
            );
            Topic topic = createTopic("topic-1", "Backend", true);
            DifficultyLevel difficultyLevel = createDifficultyLevel("difficulty-1", "Senior");

            when(topicRepository.findById("topic-1")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("difficulty-1")).thenReturn(Optional.of(difficultyLevel));
            when(studyArticleRepository.save(any(StudyArticle.class))).thenAnswer(invocation -> {
                StudyArticle studyArticle = invocation.getArgument(0);
                studyArticle.setId("article-2");
                return studyArticle;
            });

            StudyArticleResponse result = studyArticleService.create(request);

            ArgumentCaptor<StudyArticle> articleCaptor = ArgumentCaptor.forClass(StudyArticle.class);
            verify(studyArticleRepository).save(articleCaptor.capture());

            assertThat(articleCaptor.getValue().getIsActive()).isFalse();
            assertThat(result.getId()).isEqualTo("article-2");
            assertThat(result.getIsActive()).isFalse();
            assertThat(result.getDescription()).isEqualTo("A focused checklist for interview preparation");
            assertThat(result.getSource()).isEqualTo("Engineering Blog");
        }

        @Test
        @DisplayName("Should throw NotFoundException with TOP_001 when topic does not exist")
        void shouldThrowNotFoundException_whenTopicDoesNotExist() {
            StudyArticleRequest request = createRequest(
                    "missing-topic",
                    "difficulty-1",
                    "Clean Architecture",
                    "https://example.com/clean-architecture",
                    null,
                    null,
                    true
            );

            when(topicRepository.findById("missing-topic")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.TOP_001.getMessage());

            verify(topicRepository).findById("missing-topic");
            verifyNoInteractions(difficultyLevelRepository, studyArticleRepository);
        }

        @Test
        @DisplayName("Should throw InactiveResourceException with TOP_004 when topic is inactive")
        void shouldThrowInactiveResourceException_whenTopicIsInactive() {
            StudyArticleRequest request = createRequest(
                    "topic-1",
                    "difficulty-1",
                    "Domain Driven Design",
                    "https://example.com/ddd",
                    null,
                    null,
                    true
            );
            Topic inactiveTopic = createTopic("topic-1", "Backend", false);

            when(topicRepository.findById("topic-1")).thenReturn(Optional.of(inactiveTopic));

            assertThatThrownBy(() -> studyArticleService.create(request))
                    .isInstanceOf(InactiveResourceException.class)
                    .hasMessage(ErrorCode.TOP_004.getMessage());

            verify(topicRepository).findById("topic-1");
            verifyNoInteractions(difficultyLevelRepository, studyArticleRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when difficulty level does not exist")
        void shouldThrowNotFoundException_whenDifficultyLevelDoesNotExist() {
            StudyArticleRequest request = createRequest(
                    "topic-1",
                    "missing-difficulty",
                    "Refactoring Patterns",
                    "https://example.com/refactoring",
                    null,
                    null,
                    true
            );
            Topic topic = createTopic("topic-1", "Backend", true);

            when(topicRepository.findById("topic-1")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("missing-difficulty")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.DIF_001.getMessage());

            verify(topicRepository).findById("topic-1");
            verify(difficultyLevelRepository).findById("missing-difficulty");
            verifyNoInteractions(studyArticleRepository);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Should update study article when id and references are valid")
        void shouldUpdateStudyArticle_whenRequestIsValid() {
            StudyArticleRequest request = createRequest(
                    "topic-2",
                    "difficulty-2",
                    "Updated title",
                    "https://example.com/updated",
                    "Updated description",
                    "Updated source",
                    false
            );

            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("Old title")
                    .url("https://example.com/old")
                    .description("Old description")
                    .source("Old source")
                    .isActive(true)
                    .build();
            existing.setId("article-1");

            Topic topic = createTopic("topic-2", "System Design", true);
            DifficultyLevel difficultyLevel = createDifficultyLevel("difficulty-2", "Advanced");

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(topicRepository.findById("topic-2")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("difficulty-2")).thenReturn(Optional.of(difficultyLevel));

            StudyArticleResponse result = studyArticleService.update("article-1", request);

            assertThat(existing.getId()).isEqualTo("article-1");
            assertThat(existing.getTopicId()).isEqualTo("topic-2");
            assertThat(existing.getDifficultyId()).isEqualTo("difficulty-2");
            assertThat(existing.getTitle()).isEqualTo("Updated title");
            assertThat(existing.getUrl()).isEqualTo("https://example.com/updated");
            assertThat(existing.getDescription()).isEqualTo("Updated description");
            assertThat(existing.getSource()).isEqualTo("Updated source");
            assertThat(existing.getIsActive()).isFalse();

            assertThat(result.getId()).isEqualTo("article-1");
            assertThat(result.getTopicName()).isEqualTo("System Design");
            assertThat(result.getDifficultyName()).isEqualTo("Advanced");
            assertThat(result.getIsActive()).isFalse();

            verify(studyArticleRepository).findById("article-1");
            verify(topicRepository).findById("topic-2");
            verify(difficultyLevelRepository).findById("difficulty-2");
            verify(studyArticleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException with SA_001 when article does not exist")
        void shouldThrowNotFoundException_whenArticleDoesNotExist() {
            StudyArticleRequest request = createRequest(
                    "topic-1",
                    "difficulty-1",
                    "Any title",
                    "https://example.com/any",
                    null,
                    null,
                    true
            );

            when(studyArticleRepository.findById("missing-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.update("missing-id", request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.SA_001.getMessage());

            verify(studyArticleRepository).findById("missing-id");
            verifyNoInteractions(topicRepository, difficultyLevelRepository);
        }

        @Test
        @DisplayName("Should throw InactiveResourceException with TOP_004 when new topic is inactive")
        void shouldThrowInactiveResourceException_whenTopicIsInactive() {
            StudyArticleRequest request = createRequest(
                    "topic-2",
                    "difficulty-1",
                    "New title",
                    "https://example.com/new",
                    null,
                    null,
                    true
            );
            StudyArticle existing = StudyArticle.builder().topicId("topic-1").difficultyId("difficulty-1").title("Old").url("u").build();
            existing.setId("article-1");
            Topic inactiveTopic = createTopic("topic-2", "Inactive topic", false);

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(topicRepository.findById("topic-2")).thenReturn(Optional.of(inactiveTopic));

            assertThatThrownBy(() -> studyArticleService.update("article-1", request))
                    .isInstanceOf(InactiveResourceException.class)
                    .hasMessage(ErrorCode.TOP_004.getMessage());

            verify(topicRepository).findById("topic-2");
            verifyNoInteractions(difficultyLevelRepository);
            verify(studyArticleRepository, never()).save(any(StudyArticle.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException with TOP_001 when new topic does not exist")
        void shouldThrowNotFoundException_whenTopicDoesNotExist() {
            StudyArticleRequest request = createRequest(
                    "missing-topic",
                    "difficulty-1",
                    "New title",
                    "https://example.com/new",
                    null,
                    null,
                    true
            );

            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("Old")
                    .url("https://example.com/old")
                    .build();
            existing.setId("article-1");

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(topicRepository.findById("missing-topic")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.update("article-1", request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.TOP_001.getMessage());

            verify(topicRepository).findById("missing-topic");
            verifyNoInteractions(difficultyLevelRepository);
            verify(studyArticleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException with DIF_001 when difficulty level does not exist")
        void shouldThrowNotFoundException_whenDifficultyDoesNotExist() {
            StudyArticleRequest request = createRequest(
                    "topic-2",
                    "missing-difficulty",
                    "New title",
                    "https://example.com/new",
                    null,
                    null,
                    true
            );

            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("Old")
                    .url("https://example.com/old")
                    .build();
            existing.setId("article-1");

            Topic topic = createTopic("topic-2", "System Design", true);

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(topicRepository.findById("topic-2")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("missing-difficulty")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.update("article-1", request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.DIF_001.getMessage());

            verify(difficultyLevelRepository).findById("missing-difficulty");
            verify(studyArticleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should keep existing isActive when update request omits isActive")
        void shouldKeepExistingIsActive_whenIsActiveIsNull() {
            StudyArticleRequest request = createRequest(
                    "topic-2",
                    "difficulty-2",
                    "Updated title",
                    "https://example.com/updated",
                    "Updated description",
                    "Updated source",
                    null
            );

            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("Old title")
                    .url("https://example.com/old")
                    .description("Old description")
                    .source("Old source")
                    .isActive(false)
                    .build();
            existing.setId("article-1");

            Topic topic = createTopic("topic-2", "System Design", true);
            DifficultyLevel difficultyLevel = createDifficultyLevel("difficulty-2", "Advanced");

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(topicRepository.findById("topic-2")).thenReturn(Optional.of(topic));
            when(difficultyLevelRepository.findById("difficulty-2")).thenReturn(Optional.of(difficultyLevel));

            StudyArticleResponse result = studyArticleService.update("article-1", request);

            assertThat(existing.getIsActive()).isFalse();
            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Should deactivate study article when id exists")
        void shouldDeactivateStudyArticle_whenIdExists() {
            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("System Design Roadmap")
                    .url("https://example.com/system-design")
                    .description("A curated roadmap")
                    .source("Tech Blog")
                    .isActive(true)
                    .build();
            existing.setId("article-1");

            when(studyArticleRepository.findById("article-1")).thenReturn(Optional.of(existing));
            when(studyArticleRepository.save(any(StudyArticle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            studyArticleService.delete("article-1");

            ArgumentCaptor<StudyArticle> articleCaptor = ArgumentCaptor.forClass(StudyArticle.class);
            verify(studyArticleRepository).save(articleCaptor.capture());

            StudyArticle saved = articleCaptor.getValue();
            assertThat(saved.getId()).isEqualTo("article-1");
            assertThat(saved.getIsActive()).isFalse();

            verify(studyArticleRepository).findById("article-1");
        }

        @Test
        @DisplayName("Should throw NotFoundException with SA_001 when article does not exist")
        void shouldThrowNotFoundException_whenArticleDoesNotExist() {
            when(studyArticleRepository.findById("missing-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studyArticleService.delete("missing-id"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(ErrorCode.SA_001.getMessage());

            verify(studyArticleRepository).findById("missing-id");
            verify(studyArticleRepository, never()).save(any(StudyArticle.class));
            verifyNoInteractions(topicRepository, difficultyLevelRepository);
        }

        @Test
        @DisplayName("Should keep article inactive when article is already inactive")
        void shouldKeepArticleInactive_whenArticleIsAlreadyInactive() {
            StudyArticle existing = StudyArticle.builder()
                    .topicId("topic-1")
                    .difficultyId("difficulty-1")
                    .title("Old Article")
                    .url("https://example.com/old")
                    .isActive(false)
                    .build();
            existing.setId("article-2");

            when(studyArticleRepository.findById("article-2")).thenReturn(Optional.of(existing));
            when(studyArticleRepository.save(any(StudyArticle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            studyArticleService.delete("article-2");

            ArgumentCaptor<StudyArticle> articleCaptor = ArgumentCaptor.forClass(StudyArticle.class);
            verify(studyArticleRepository).save(articleCaptor.capture());

            StudyArticle saved = articleCaptor.getValue();
            assertThat(saved.getId()).isEqualTo("article-2");
            assertThat(saved.getIsActive()).isFalse();
        }
    }

    private StudyArticleDetailProjection createProjection(
            String id,
            String topicId,
            String topicName,
            String difficultyId,
            String difficultyName,
            String title,
            String url,
            String description,
            String source,
            Boolean isActive
    ) {
        return new TestStudyArticleDetailProjection(
                id,
                topicId,
                topicName,
                difficultyId,
                difficultyName,
                title,
                url,
                description,
                source,
                isActive
        );
    }

    private StudyArticleRequest createRequest(
            String topicId,
            String difficultyId,
            String title,
            String url,
            String description,
            String source,
            Boolean isActive
    ) {
        return StudyArticleRequest.builder()
                .topicId(topicId)
                .difficultyId(difficultyId)
                .title(title)
                .url(url)
                .description(description)
                .source(source)
                .isActive(isActive)
                .build();
    }

    private Topic createTopic(String id, String name, boolean isActive) {
        Topic topic = Topic.builder()
                .name(name)
                .isActive(isActive)
                .build();
        topic.setId(id);
        return topic;
    }

    private DifficultyLevel createDifficultyLevel(String id, String name) {
        DifficultyLevel difficultyLevel = DifficultyLevel.builder()
                .name(name)
                .weight(50)
                .build();
        difficultyLevel.setId(id);
        return difficultyLevel;
    }

    private StudyArticle createStudyArticle(
            String id,
            String topicId,
            String difficultyId,
            String title,
            String url,
            String description,
            String source,
            boolean isActive
    ) {
        StudyArticle studyArticle = StudyArticle.builder()
                .topicId(topicId)
                .difficultyId(difficultyId)
                .title(title)
                .url(url)
                .description(description)
                .source(source)
                .isActive(isActive)
                .build();
        studyArticle.setId(id);
        return studyArticle;
    }

    private static final class TestStudyArticleDetailProjection implements StudyArticleDetailProjection {
        private final String id;
        private final String topicId;
        private final String topicName;
        private final String difficultyId;
        private final String difficultyName;
        private final String title;
        private final String url;
        private final String description;
        private final String source;
        private final Boolean isActive;

        private TestStudyArticleDetailProjection(
                String id,
                String topicId,
                String topicName,
                String difficultyId,
                String difficultyName,
                String title,
                String url,
                String description,
                String source,
                Boolean isActive
        ) {
            this.id = id;
            this.topicId = topicId;
            this.topicName = topicName;
            this.difficultyId = difficultyId;
            this.difficultyName = difficultyName;
            this.title = title;
            this.url = url;
            this.description = description;
            this.source = source;
            this.isActive = isActive;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getTopicId() {
            return topicId;
        }

        @Override
        public String getTopicName() {
            return topicName;
        }

        @Override
        public String getDifficultyId() {
            return difficultyId;
        }

        @Override
        public String getDifficultyName() {
            return difficultyName;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public Boolean getIsActive() {
            return isActive;
        }
    }
}
