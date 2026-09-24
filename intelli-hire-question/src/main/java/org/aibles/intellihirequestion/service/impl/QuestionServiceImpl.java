package org.aibles.intellihirequestion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.*;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestion;
import org.aibles.intellihirequestion.dto.ai.AiGeneratedQuestionResult;
import org.aibles.intellihirequestion.dto.resume.ResumeAggregateData;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.entity.Question;
import org.aibles.intellihirequestion.entity.QuestionTag;
import org.aibles.intellihirequestion.entity.Tag;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.entity.enums.QuestionType;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.InactiveResourceException;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.QuestionMapper;
import org.aibles.intellihirequestion.mapper.TagMapper;
import org.aibles.intellihirequestion.projection.QuestionDetailProjection;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.QuestionTagRepository;
import org.aibles.intellihirequestion.repository.RubricRepository;
import org.aibles.intellihirequestion.repository.TagRepository;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.aibles.intellihirequestion.service.QuestionService;
import org.aibles.intellihirequestion.service.ai.QuestionGenerationAiClient;
import org.aibles.intellihirequestion.service.client.ResumeAggregateClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.aibles.intellihirequestion.specification.QuestionSpecification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.aibles.intellihirequestion.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final String RANDOM_QUESTION_CACHE_PREFIX = "questions:random:";
    private static final long RANDOM_QUESTION_CACHE_TTL = 5;

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final DifficultyLevelRepository difficultyLevelRepository;
    private final TagRepository tagRepository;
    private final QuestionTagRepository questionTagRepository;
    private final RubricRepository rubricRepository;
    private final QuestionMapper questionMapper;
    private final TagMapper tagMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ResumeAggregateClient resumeAggregateClient;
    private final QuestionGenerationAiClient questionGenerationAiClient;

    /**
     * Creates a new question based on the provided request.
     *
     * @param request the question request containing details for the new question
     * @return the created question response
     */
    @Override
    @Transactional
    public QuestionResponse create(QuestionRequest request, String userId, String username) {
        log.info("QuestionServiceImpl.create - Creating question with topicId: {}, difficultyId: {}",
                request.getTopicId(), request.getDifficultyId());

        // Validate topic exists and is active
        Topic topic = getTopic(request.getTopicId());
        // Validate difficulty level exists
        DifficultyLevel difficultyLevel = getDifficultyLevel(request.getDifficultyId());
        // Validate all tags exist
        List<Tag> tags = getValidTags(request.getTagIds());
        // Create and save question
        Question question = saveQuestion(request, userId, username);
        // Save question-tag relationships
        saveQuestionTag(tags, question);

        return getResponse(question, topic, difficultyLevel, tags);
    }

    private Question saveQuestion(QuestionRequest request, String userId, String username) {
        Question question = questionMapper.toEntity(request);
        question.setStatus("PENDING");
        if (userId != null) {
            try { question.setSubmittedById(Long.parseLong(userId)); } catch (NumberFormatException ignored) {}
        }
        if (username != null) {
            question.setSubmittedBy(username);
        }
        question.setCreatedAt(LocalDateTime.now());
        question = questionRepository.save(question);
        log.info("Question created with ID: {}", question.getId());
        return question;
    }

    private void saveQuestionTag(List<Tag> tags, Question question) {
        if (!tags.isEmpty()) {
            final String questionId = question.getId();
            List<QuestionTag> questionTags = tags.stream()
                    .map(tag -> new QuestionTag(questionId, tag.getId()))
                    .collect(Collectors.toList());
            questionTagRepository.saveAll(questionTags);
            log.info("Saved {} question-tag relationships for question ID: {}",
                    questionTags.size(), questionId);
        }
    }

    private DifficultyLevel getDifficultyLevel(String difficultyId) {
        return difficultyLevelRepository.findById(difficultyId)
                .orElseThrow(() -> {
                    log.error("Difficulty level not found with ID: {}", difficultyId);
                    return new NotFoundException(ErrorCode.DIF_001);
                });
    }

    private Topic getTopic(String topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> {
                    log.error("Topic not found with ID: {}", topicId);
                    return new NotFoundException(ErrorCode.TOP_001);
                });

        if (Boolean.FALSE.equals(topic.getIsActive())) {
            log.error("Topic is inactive with ID: {}", topicId);
            throw new InactiveResourceException(ErrorCode.TOP_004);
        }
        return topic;
    }

    /**
     * Soft deletes a question by setting isActive = false.
     *
     * @param id the question ID to delete
     */
    @Override
    @Transactional
    public void delete(String id) {
        log.info("Soft deleting question with id: {}", id);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new NotFoundException(ErrorCode.QUE_001);
                });

        question.setIsActive(false);
        questionRepository.save(question);

        // Soft delete rubric if exists
        rubricRepository.findByQuestionId(id)
                .ifPresent(rubric -> {
                    rubric.setIsActive(false);
                    rubricRepository.save(rubric);
                    log.info("Soft deleted rubric for question id: {}", id);
                });

        log.info("Successfully soft deleted question with id: {}", id);
    }

    /**
     * Updates an existing question.
     *
     * @param id      the ID of the question to update
     * @param request the request object containing updated question details
     * @return the updated question response
     */
    @Override
    @Transactional
    public QuestionResponse update(String id, QuestionRequest request, String updatedBy) {
        log.info("QuestionServiceImpl.update - Updating question with id: {}, topicId: {}, difficultyId: {}", id, request.getTopicId(), request.getDifficultyId());

        // 1. Load and validate entities
        Question question = getQuestionOrThrow(id);
        Topic topic = getTopic(request.getTopicId());
        DifficultyLevel difficultyLevel = getDifficultyLevel(request.getDifficultyId());
        List<Tag> tags = getValidTags(request.getTagIds());

        // 2. Apply updates and save
        applyQuestionUpdates(question, request, updatedBy);
        question = questionRepository.save(question);
        log.info("Question updated with ID: {}", question.getId());

        // 3. Update tags mapping
        updateQuestionTags(question.getId(), tags);

        return getResponse(question, topic, difficultyLevel, tags);
    }

    private QuestionResponse getResponse(Question question, Topic topic, DifficultyLevel difficultyLevel, List<Tag> tags) {
        return questionMapper.toResponse(
                question,
                topic.getName(),
                difficultyLevel.getName(),
                tagMapper.toResponseList(tags)
        );
    }

    private Question getQuestionOrThrow(String id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with ID: {}", id);
                    return new NotFoundException(ErrorCode.QUE_001);
                });
    }

    private List<Tag> getValidTags(List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Tag> tags = tagRepository.findByIdIn(tagIds);
        if (tags.size() != tagIds.size()) {
            log.error("One or more tags not found. Expected: {}, Found: {}", tagIds.size(), tags.size());
            throw new InactiveResourceException(ErrorCode.TAG_004);
        }

        return tags;
    }

    private void applyQuestionUpdates(Question question, QuestionRequest request, String updatedBy) {
        question.setTopicId(request.getTopicId());
        question.setDifficultyId(request.getDifficultyId());
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setIsActive(request.getIsActive());
        question.setLastUpdatedAt(LocalDateTime.now());
        if (updatedBy != null) {
            question.setLastUpdatedBy(updatedBy);
        }
    }

    private void updateQuestionTags(String questionId, List<Tag> tags) {
        questionTagRepository.deleteByQuestionId(questionId);

        if (!tags.isEmpty()) {
            List<QuestionTag> questionTags = tags.stream()
                    .map(tag -> new QuestionTag(questionId, tag.getId()))
                    .collect(Collectors.toList());
            questionTagRepository.saveAll(questionTags);
            log.info("Updated {} question-tag relationships for question ID: {}",
                    questionTags.size(), questionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getById(String id) {
        log.info("Get question by id: {}", id);

        // Query 1: Get question with topic and difficulty using Projection
        QuestionDetailProjection projection = questionRepository.findByIdProjection(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new NotFoundException(ErrorCode.QUE_001);
                });

        // Query 2: Get tags for this question
        List<Tag> tags = tagRepository.findByQuestionId(id);

        log.info("Successfully retrieved question with id: {}", id);
        return buildResponse(projection, tags);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getRandomQuestions(RandomQuestionRequest request) {
        log.info("Getting random questions - topicId: {}, difficultyId: {}, type: {}, count: {}",
                request.getTopicId(), request.getDifficultyId(), request.getType(), request.getCount());

        // 1. Validate references if provided
        if (request.getTopicId() != null) {
            topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> {
                        log.error("Topic not found with ID: {}", request.getTopicId());
                        return new NotFoundException(ErrorCode.TOP_001);
                    });
        }
        if (request.getDifficultyId() != null) {
            difficultyLevelRepository.findById(request.getDifficultyId())
                    .orElseThrow(() -> {
                        log.error("Difficulty level not found with ID: {}", request.getDifficultyId());
                        return new NotFoundException(ErrorCode.DIF_001);
                    });
        }

        // 2. Parse question type if provided
        QuestionType questionType = parseQuestionType(request.getType());

        // 3. Build cache key and check Redis
        String cacheKey = buildCacheKey(request);
        List<String> questionIds = getFromCache(cacheKey);

        if (questionIds == null) {
            // 4. Cache miss - query from DB
            questionIds = queryQuestionIds(request, questionType);
            // 5. Cache in Redis with TTL 5 minutes
            putInCache(cacheKey, questionIds);
            log.info("Cache MISS for key: {}, found {} question IDs", cacheKey, questionIds.size());
        } else {
            log.info("Cache HIT for key: {}, found {} question IDs", cacheKey, questionIds.size());
        }

        // 6. Remove excludeIds from pool
        if (request.getExcludeIds() != null && !request.getExcludeIds().isEmpty()) {
            questionIds = new ArrayList<>(questionIds);
            questionIds.removeAll(request.getExcludeIds());
        }

        // 7. Validate enough questions available
        if (questionIds.size() < request.getCount()) {
            log.error("Not enough questions available. Found {} but requested {}",
                    questionIds.size(), request.getCount());
            throw new NotFoundException("QUE_005",
                    String.format("Không đủ câu hỏi khả dụng. Tìm thấy %d nhưng yêu cầu %d",
                            questionIds.size(), request.getCount()));
        }

        // 8. Shuffle and take first N
        List<String> selectedIds = new ArrayList<>(questionIds);
        Collections.shuffle(selectedIds);
        selectedIds = selectedIds.subList(0, request.getCount());

        // 9. Fetch full question data
        List<Question> questions = questionRepository.findAllById(selectedIds);

        // 10. Batch fetch topic names and difficulty names
        Set<String> topicIds = questions.stream().map(Question::getTopicId).collect(Collectors.toSet());
        Set<String> difficultyIds = questions.stream().map(Question::getDifficultyId).collect(Collectors.toSet());

        Map<String, String> topicNames = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        // 11. Batch fetch tags (avoid N+1)
        List<String> fetchedIds = questions.stream().map(Question::getId).toList();
        List<QuestionTag> allQuestionTags = questionTagRepository.findByQuestionIdIn(fetchedIds);
        Set<String> allTagIds = allQuestionTags.stream().map(QuestionTag::getTagId).collect(Collectors.toSet());
        Map<String, Tag> tagMap = tagRepository.findAllById(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
        Map<String, List<Tag>> tagsByQuestionId = allQuestionTags.stream()
                .filter(qt -> tagMap.containsKey(qt.getTagId()))
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(qt -> tagMap.get(qt.getTagId()), Collectors.toList())
                ));

        // 12. Map to response
        return questions.stream()
                .map(question -> questionMapper.toResponse(
                        question,
                        topicNames.getOrDefault(question.getTopicId(), ""),
                        difficultyNames.getOrDefault(question.getDifficultyId(), ""),
                        tagMapper.toResponseList(tagsByQuestionId.getOrDefault(question.getId(), List.of()))
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GenerateQuestionsFromResumeResponse generateFromResume(String userId, GenerateQuestionsFromResumeRequest request) {
        log.info("Generating questions from resumeId={} for user={}", request.getResumeId(), userId);

        QuestionType preferredType = parseQuestionType(request.getQuestionType());
        ResumeAggregateData resumeData = resumeAggregateClient.getAggregate(userId, request.getResumeId());

        List<Topic> topics = topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc();
        List<DifficultyLevel> difficulties = difficultyLevelRepository.findAllByOrderByWeightAsc();

        if (topics.isEmpty()) {
            log.warn("No active topics configured in question-service; generating resume-based questions without topic catalog");
        }
        if (difficulties.isEmpty()) {
            log.warn("No difficulty levels configured in question-service; generating resume-based questions without difficulty catalog");
        }

        AiGeneratedQuestionResult aiResult = questionGenerationAiClient.generateQuestions(resumeData, request, topics, difficulties);

        Map<String, Topic> topicByName = topics.stream()
                .collect(Collectors.toMap(topic -> topic.getName().trim().toLowerCase(), topic -> topic, (left, right) -> left));
        Map<String, DifficultyLevel> difficultyByName = difficulties.stream()
                .collect(Collectors.toMap(diff -> diff.getName().trim().toLowerCase(), diff -> diff, (left, right) -> left));

        List<AiGeneratedQuestion> aiQuestions = aiResult.getQuestions() != null
                ? aiResult.getQuestions()
                : List.of();

        List<GeneratedQuestionItemResponse> generatedQuestions = aiQuestions
                .stream()
                .map(item -> toGeneratedQuestionResponse(item, preferredType, topicByName, difficultyByName))
                .toList();

        return GenerateQuestionsFromResumeResponse.builder()
                .resumeId(request.getResumeId())
                .candidateSummary(aiResult.getCandidateSummary())
                .totalQuestions(generatedQuestions.size())
                .generatedAt(LocalDateTime.now())
                .questions(generatedQuestions)
                .build();
    }

    private QuestionType parseQuestionType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid question type: {}", type);
            throw new BadRequestException(ErrorCode.QUE_003);
        }
    }

    private List<String> queryQuestionIds(RandomQuestionRequest request, QuestionType questionType) {
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            return questionRepository.findIdsByFiltersWithTags(
                    request.getTopicId(), request.getDifficultyId(), questionType, request.getTagIds());
        }
        return questionRepository.findIdsByFilters(
                request.getTopicId(), request.getDifficultyId(), questionType);
    }

    private GeneratedQuestionItemResponse toGeneratedQuestionResponse(
            AiGeneratedQuestion item,
            QuestionType preferredType,
            Map<String, Topic> topicByName,
            Map<String, DifficultyLevel> difficultyByName) {
        String normalizedTopicName = normalizeLookup(item.getTopicName());
        Topic topic = normalizedTopicName != null ? topicByName.get(normalizedTopicName) : null;

        String normalizedDifficultyName = normalizeLookup(item.getDifficultyName());
        DifficultyLevel difficulty = normalizedDifficultyName != null ? difficultyByName.get(normalizedDifficultyName) : null;

        QuestionType generatedType = preferredType;
        if (generatedType == null) {
            try {
                generatedType = QuestionType.valueOf(item.getQuestionType().trim().toUpperCase());
            } catch (Exception ex) {
                generatedType = QuestionType.TECHNICAL;
            }
        }

        return GeneratedQuestionItemResponse.builder()
                .questionText(item.getQuestionText())
                .questionType(generatedType.name())
                .topicId(topic != null ? topic.getId() : null)
                .topicName(topic != null ? topic.getName() : item.getTopicName())
                .difficultyId(difficulty != null ? difficulty.getId() : null)
                .difficultyName(difficulty != null ? difficulty.getName() : item.getDifficultyName())
                .rationale(item.getRationale())
                .build();
    }

    private String normalizeLookup(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String buildCacheKey(RandomQuestionRequest request) {
        StringBuilder key = new StringBuilder(RANDOM_QUESTION_CACHE_PREFIX);
        key.append(request.getTopicId() != null ? request.getTopicId() : "all");
        key.append(":");
        key.append(request.getDifficultyId() != null ? request.getDifficultyId() : "all");
        key.append(":");
        key.append(request.getType() != null ? request.getType().toUpperCase() : "all");
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<String> sortedTags = new ArrayList<>(request.getTagIds());
            Collections.sort(sortedTags);
            key.append(":").append(String.join(",", sortedTags));
        }
        return key.toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> getFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<String>) cached;
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to get from Redis cache: {}", e.getMessage());
            return null;
        }
    }

    private void putInCache(String cacheKey, List<String> ids) {
        try {
            redisTemplate.opsForValue().set(cacheKey, ids, RANDOM_QUESTION_CACHE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to put in Redis cache: {}", e.getMessage());
        }
    }

    private QuestionResponse buildResponse(QuestionDetailProjection projection, List<Tag> tags) {
        List<TagResponse> tagResponses = tags.stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .toList();

        return QuestionResponse.builder()
                .id(projection.getId())
                .topicId(projection.getTopicId())
                .topicName(projection.getTopicName())
                .difficultyId(projection.getDifficultyId())
                .difficultyName(projection.getDifficultyName())
                .questionText(projection.getQuestionText())
                .questionType(String.valueOf(QuestionType.valueOf(projection.getQuestionType())))
                .isActive(projection.getIsActive())
                .status(projection.getStatus())
                .submittedBy(projection.getSubmittedBy())
                .submittedById(projection.getSubmittedById())
                .rejectionReason(projection.getRejectionReason())
                .createdAt(projection.getCreatedAt())
                .lastUpdatedAt(projection.getLastUpdatedAt())
                .tags(tagResponses)
                .build();
    }

    private QuestionResponse buildFullResponse(Question question) {
        Topic topic = topicRepository.findById(question.getTopicId()).orElse(null);
        DifficultyLevel dl = difficultyLevelRepository.findById(question.getDifficultyId()).orElse(null);
        List<Tag> tags = tagRepository.findByQuestionId(question.getId());
        return questionMapper.toResponse(
                question,
                topic != null ? topic.getName() : "",
                dl != null ? dl.getName() : "",
                tagMapper.toResponseList(tags)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> adminListQuestions(AdminQuestionSearchRequest request, Pageable pageable) {
        log.info("adminListQuestions - status: {}, topicId: {}, type: {}, keyword: {}",
                request.getStatus(), request.getTopicId(), request.getType(), request.getKeyword());

        Specification<Question> spec = Specification
                .where(QuestionSpecification.withStatus(request.getStatus()))
                .and(QuestionSpecification.withTopicId(request.getTopicId()))
                .and(QuestionSpecification.withType(request.getType()))
                .and(QuestionSpecification.withKeyword(request.getKeyword()))
                .and(QuestionSpecification.withSubmittedBy(request.getSubmittedBy()));

        Page<Question> questionPage = questionRepository.findAll(spec, pageable);

        Set<String> topicIds = questionPage.getContent().stream().map(Question::getTopicId).collect(Collectors.toSet());
        Set<String> difficultyIds = questionPage.getContent().stream().map(Question::getDifficultyId).collect(Collectors.toSet());

        Map<String, String> topicNames = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        Set<String> questionIds = questionPage.getContent().stream().map(Question::getId).collect(Collectors.toSet());
        List<QuestionTag> allQuestionTags = questionTagRepository.findByQuestionIdIn(new ArrayList<>(questionIds));
        Set<String> allTagIds = allQuestionTags.stream().map(QuestionTag::getTagId).collect(Collectors.toSet());
        Map<String, Tag> tagMap = tagRepository.findAllById(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
        Map<String, List<Tag>> tagsByQuestionId = allQuestionTags.stream()
                .filter(qt -> tagMap.containsKey(qt.getTagId()))
                .collect(Collectors.groupingBy(QuestionTag::getQuestionId,
                        Collectors.mapping(qt -> tagMap.get(qt.getTagId()), Collectors.toList())));

        Page<QuestionResponse> responsePage = questionPage.map(question -> {
            List<Tag> tags = tagsByQuestionId.getOrDefault(question.getId(), List.of());
            return questionMapper.toResponse(question,
                    topicNames.getOrDefault(question.getTopicId(), ""),
                    difficultyNames.getOrDefault(question.getDifficultyId(), ""),
                    tagMapper.toResponseList(tags));
        });

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse adminGetById(String id) {
        return getById(id);
    }

    @Override
    @Transactional
    public QuestionResponse adminPatchModerationStatus(String id, String status, String rejectionReason) {
        log.info("adminPatchModerationStatus - id: {}, status: {}", id, status);
        Question question = getQuestionOrThrow(id);
        question.setStatus(status);
        if ("REJECTED".equals(status)) {
            question.setRejectionReason(rejectionReason);
        } else {
            question.setRejectionReason(null);
        }
        question.setLastUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
        return buildFullResponse(question);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> getSubmittedQuestions(Long userId, Pageable pageable) {
        log.info("getSubmittedQuestions - userId: {}", userId);
        Specification<Question> spec = QuestionSpecification.withSubmittedById(userId);
        Page<Question> questionPage = questionRepository.findAll(spec, pageable);

        Set<String> topicIds = questionPage.getContent().stream().map(Question::getTopicId).collect(Collectors.toSet());
        Set<String> difficultyIds = questionPage.getContent().stream().map(Question::getDifficultyId).collect(Collectors.toSet());
        Map<String, String> topicNames = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        Page<QuestionResponse> responsePage = questionPage.map(question -> {
            List<Tag> tags = tagRepository.findByQuestionId(question.getId());
            return questionMapper.toResponse(question,
                    topicNames.getOrDefault(question.getTopicId(), ""),
                    difficultyNames.getOrDefault(question.getDifficultyId(), ""),
                    tagMapper.toResponseList(tags));
        });

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public QuestionResponse resubmit(String id, Long userId) {
        log.info("resubmit - id: {}, userId: {}", id, userId);
        Question question = getQuestionOrThrow(id);
        if (!Objects.equals(question.getSubmittedById(), userId)) {
            throw new BadRequestException("QUE_403", "Bạn không phải là chủ sở hữu của câu hỏi này");
        }
        if (!"REJECTED".equals(question.getStatus())) {
            throw new BadRequestException("QUE_409", "Chỉ có thể gửi lại các câu hỏi ở trạng thái REJECTED");
        }
        question.setStatus("PENDING");
        question.setRejectionReason(null);
        question.setLastUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
        return buildFullResponse(question);
    }

    @Override
    @Transactional
    public BulkImportResponse bulkImport(BulkImportRequest request) {
        log.info("bulkImport - Starting bulk import of {} questions", request.getQuestions().size());

        List<QuestionRequest> questionRequests = request.getQuestions();
        int total = questionRequests.size();
        List<BulkImportError> errors = new ArrayList<>();
        List<QuestionRequest> validRequests = new ArrayList<>();
        List<Integer> validIndexes = new ArrayList<>();

        // 1. Collect all IDs from the batch
        Set<String> allTopicIds = new HashSet<>();
        Set<String> allDifficultyIds = new HashSet<>();
        Set<String> allTagIds = new HashSet<>();

        for (QuestionRequest req : questionRequests) {
            allTopicIds.add(req.getTopicId());
            allDifficultyIds.add(req.getDifficultyId());
            if (req.getTagIds() != null) {
                allTagIds.addAll(req.getTagIds());
            }
        }

        // 2. Batch query DB (3 queries total instead of N*3)
        Map<String, Topic> existingTopics = topicRepository.findAllById(allTopicIds).stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));
        Map<String, DifficultyLevel> existingDifficulties = difficultyLevelRepository.findAllById(allDifficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, d -> d));
        Set<String> existingTagIds = tagRepository.findAllById(allTagIds).stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        // 3. Check duplicate questionText within batch
        Set<String> seenTexts = new HashSet<>();

        // 4. Validate each question and classify valid/invalid
        for (int i = 0; i < questionRequests.size(); i++) {
            QuestionRequest req = questionRequests.get(i);
            String error = validateQuestionInBatch(req, existingTopics, existingDifficulties, existingTagIds, seenTexts);

            if (error != null) {
                errors.add(new BulkImportError(i, error));
            } else {
                seenTexts.add(req.getQuestionText().trim().toLowerCase());
                validRequests.add(req);
                validIndexes.add(i);
            }
        }

        // 5. Save all valid questions
        int created = 0;
        if (!validRequests.isEmpty()) {
            String currentUser = SecurityUtils.getCurrentUser();
            LocalDateTime now = LocalDateTime.now();

            List<Question> questions = validRequests.stream()
                    .map(req -> {
                        Question q = questionMapper.toEntity(req);
                        q.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
                        q.setStatus("PENDING");
                        q.setCreatedBy(currentUser);
                        q.setCreatedAt(now);
                        return q;
                    })
                    .collect(Collectors.toList());

            List<Question> savedQuestions = questionRepository.saveAll(questions);
            created = savedQuestions.size();

            // 6. Batch insert question_tags
            List<QuestionTag> allQuestionTags = new ArrayList<>();
            for (int i = 0; i < savedQuestions.size(); i++) {
                Question savedQuestion = savedQuestions.get(i);
                QuestionRequest req = validRequests.get(i);
                if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
                    for (String tagId : req.getTagIds()) {
                        allQuestionTags.add(new QuestionTag(savedQuestion.getId(), tagId));
                    }
                }
            }
            if (!allQuestionTags.isEmpty()) {
                questionTagRepository.saveAll(allQuestionTags);
            }

            // 7. Invalidate cache
            invalidateRandomQuestionCache();

            log.info("bulkImport - Successfully created {} questions", created);
        }

        // 8. Build response
        return BulkImportResponse.builder()
                .total(total)
                .created(created)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private String validateQuestionInBatch(
            QuestionRequest req,
            Map<String, Topic> existingTopics,
            Map<String, DifficultyLevel> existingDifficulties,
            Set<String> existingTagIds,
            Set<String> seenTexts) {

        // Check topic exists
        Topic topic = existingTopics.get(req.getTopicId());
        if (topic == null) {
            return "Không tìm thấy chủ đề: " + req.getTopicId();
        }

        // Check topic is active
        if (Boolean.FALSE.equals(topic.getIsActive())) {
            return "Chủ đề đang không hoạt động: " + req.getTopicId();
        }

        // Check difficulty exists
        if (!existingDifficulties.containsKey(req.getDifficultyId())) {
            return "Không tìm thấy độ khó: " + req.getDifficultyId();
        }

        // Check all tags exist
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            for (String tagId : req.getTagIds()) {
                if (!existingTagIds.contains(tagId)) {
                    return "Không tìm thấy nhãn: " + tagId;
                }
            }
        }

        // Check duplicate questionText within batch
        String normalizedText = req.getQuestionText().trim().toLowerCase();
        if (seenTexts.contains(normalizedText)) {
            return "Trùng lặp nội dung câu hỏi trong lô nhập";
        }

        return null;
    }

    private void invalidateRandomQuestionCache() {
        try {
            Set<String> keys = redisTemplate.keys(RANDOM_QUESTION_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated {} random question cache entries", keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate cache: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> getByTopicId(String topicId, Pageable pageable) {
        log.info("getByTopicId - topicId: {}", topicId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TOP_001));

        if (Boolean.FALSE.equals(topic.getIsActive())) {
            throw new InactiveResourceException(ErrorCode.TOP_004);
        }

        Page<Question> questionPage = questionRepository.findByTopicIdAndIsActiveTrue(topicId, pageable);

        Set<String> difficultyIds = questionPage.getContent().stream()
                .map(Question::getDifficultyId).collect(Collectors.toSet());

        Map<String, String> topicNames = Map.of(topic.getId(), topic.getName());
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        List<String> questionIds = questionPage.getContent().stream()
                .map(Question::getId).toList();
        List<QuestionTag> allQuestionTags = questionTagRepository.findByQuestionIdIn(questionIds);

        Set<String> allTagIds = allQuestionTags.stream()
                .map(QuestionTag::getTagId).collect(Collectors.toSet());
        Map<String, Tag> tagMap = tagRepository.findAllById(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        Map<String, List<Tag>> tagsByQuestionId = allQuestionTags.stream()
                .filter(qt -> tagMap.containsKey(qt.getTagId()))
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(qt -> tagMap.get(qt.getTagId()), Collectors.toList())
                ));

        Page<QuestionResponse> responsePage = questionPage.map(question -> {
            List<Tag> tags = tagsByQuestionId.getOrDefault(question.getId(), List.of());
            return questionMapper.toResponse(
                    question,
                    topicNames.getOrDefault(question.getTopicId(), ""),
                    difficultyNames.getOrDefault(question.getDifficultyId(), ""),
                    tagMapper.toResponseList(tags)
            );
        });

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public long countQuestions(String topicId, String difficultyId) {
        log.info("Counting active questions - topicId: {}, difficultyId: {}", topicId, difficultyId);
        return questionRepository.countByFilters(topicId, difficultyId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> searchQuestion(QuestionSearchRequest request, Pageable pageable) {
        log.info("Searching questions - keyword: {}, topicId: {}, difficultyId: {}, type: {}",
                request.getKeyword(), request.getTopicId(), request.getDifficultyId(), request.getType());

        // 1. Build Specification from filters
        Specification<Question> spec = Specification
                .where(QuestionSpecification.withTopicId(request.getTopicId()))
                .and(QuestionSpecification.withDifficultyId(request.getDifficultyId()))
                .and(QuestionSpecification.withType(request.getType()))
                .and(QuestionSpecification.withIsActive(request.getIsActive()))
                .and(QuestionSpecification.withStatus(request.getStatus()))
                .and(QuestionSpecification.withKeyword(request.getKeyword()))
                .and(QuestionSpecification.withTagIds(request.getTagIds()));

        // 2. Query with specification + pagination
        Page<Question> questionPage = questionRepository.findAll(spec, pageable);

        // 3. Batch fetch topic names and difficulty names to avoid N+1
        Set<String> topicIds = questionPage.getContent().stream()
                .map(Question::getTopicId).collect(Collectors.toSet());
        Set<String> difficultyIds = questionPage.getContent().stream()
                .map(Question::getDifficultyId).collect(Collectors.toSet());

        Map<String, String> topicNames = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));
        Map<String, String> difficultyNames = difficultyLevelRepository.findAllById(difficultyIds).stream()
                .collect(Collectors.toMap(DifficultyLevel::getId, DifficultyLevel::getName));

        // 4. Batch fetch tags for all questions (fix N+1)
        List<String> questionIds = questionPage.getContent().stream()
                .map(Question::getId).toList();
        List<QuestionTag> allQuestionTags = questionTagRepository.findByQuestionIdIn(questionIds);

        Set<String> allTagIds = allQuestionTags.stream()
                .map(QuestionTag::getTagId).collect(Collectors.toSet());
        Map<String, Tag> tagMap = tagRepository.findAllById(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        Map<String, List<Tag>> tagsByQuestionId = allQuestionTags.stream()
                .filter(qt -> tagMap.containsKey(qt.getTagId()))
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(qt -> tagMap.get(qt.getTagId()), Collectors.toList())
                ));

        // 5. Map Question -> QuestionResponse
        Page<QuestionResponse> responsePage = questionPage.map(question -> {
            List<Tag> tags = tagsByQuestionId.getOrDefault(question.getId(), List.of());
            return questionMapper.toResponse(
                    question,
                    topicNames.getOrDefault(question.getTopicId(), ""),
                    difficultyNames.getOrDefault(question.getDifficultyId(), ""),
                    tagMapper.toResponseList(tags)
            );
        });

        log.info("Search completed - found {} results, page {}/{}",
                responsePage.getTotalElements(), pageable.getPageNumber(), responsePage.getTotalPages());

        // 6. Convert Spring Page to custom PageResponse DTO
        return PageResponse.from(responsePage);
    }
}
