package org.aibles.intellihirequestion.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.TopicRequest;
import org.aibles.intellihirequestion.dto.TopicResponse;
import org.aibles.intellihirequestion.dto.TopicTreeResponse;
import org.aibles.intellihirequestion.entity.Topic;
import org.aibles.intellihirequestion.exception.*;
import org.aibles.intellihirequestion.mapper.TopicMapper;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.repository.TopicRepository;
import org.aibles.intellihirequestion.service.TopicService;
import org.aibles.intellihirequestion.utils.SecurityUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import static org.aibles.intellihirequestion.exception.ErrorCode.TOP_001;
import static org.aibles.intellihirequestion.mapper.TopicMapper.toResponse;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final TopicMapper topicMapper;

    /**
     * Get all topics.
     *
     * @return list of TopicResponse
     */
    @Override
    @Cacheable(value = "topics:all")
    public List<TopicResponse> getAll() {
        log.info("Cache miss for getAll topics, fetching from database.");
        return topicMapper.topicResponseList(topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc());
    }

    /** Retrieves all active topics as nested tree structure with caching. */
    @Cacheable(value = "topics:tree", unless = "#result.isEmpty()")
    @Override
    public List<TopicTreeResponse> getTree() {
        log.info("Cache MISS - Fetching topics from database");

        var topics = topicRepository.findByIsActiveTrueOrderByOrderAscNameAsc();
        log.info("Found {} active topics", topics.size());

        return buildTopicsTree(topics);
    }

    /**
     * Create a new topic.
     *
     * @param request the topic request
     * @return the created topic response
     */
    @Override
    @CacheEvict(value = {"topics:all", "topics:tree"}, allEntries = true)
    public TopicResponse create(TopicRequest request) {
        log.info("Creating new topic with name: {}", request.getName());

        // Validate parent exists and is active (if parentId provided)
        validateParent(request.getParentId());

        // Validate name is unique under same parent
        validateNameUnique(request.getName(), request.getParentId());

        // Build and save entity
        Topic topic = topicMapper.toEntity(request);
        Topic savedTopic = topicRepository.save(topic);

        log.info("Created topic with id: {}", savedTopic.getId());
        return TopicMapper.toResponse(savedTopic);
    }

    /**
     * Delete a topic by setting isActive to false (soft delete).
     *
     * @param id the ID of the topic to delete
     */
    @Override
    @CacheEvict(value = {"topics:all", "topics:tree"}, allEntries = true)
    @Transactional
    public void delete(String id) {
        log.info("Deleting topic with id: {}", id);
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic not found with id: {}", id);
                    return new NotFoundException(ErrorCode.TOP_001);
                });

        if (!Boolean.TRUE.equals(topic.getIsActive())) {
            log.error("Topic with id: {} is already inactive", id);
            throw new NotFoundException(ErrorCode.TOP_001);
        }

        if (topicRepository.existsByParentIdAndIsActiveTrue(topic.getId())) {
            log.error("Cannot delete topic with id: {} because it has active child topics", id);
            throw new BadRequestException(ErrorCode.TOP_004);
        }

        if (questionRepository.existsByTopicIdAndIsActiveTrue(topic.getId())) {
            log.error("Cannot delete topic with id: {} because it has active questions", id);
            throw new BadRequestException(ErrorCode.TOP_005);
        }

        String currentUser = SecurityUtils.getCurrentUser();
        topic.setIsActive(false);
        topic.setLastUpdatedBy(currentUser);
        topic.setLastUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);
        log.info("Successfully deleted topic with id: {}", id);
    }

    /**
     * Validate parent topic exists and is active.
     */
    private void validateParent(String parentId) {
        if (parentId == null) {
            return;
        }

        Topic parent = topicRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Parent topic not found with id: {}", parentId);
                    return new NotFoundException(ErrorCode.TOP_001);
                });

        if (!Boolean.TRUE.equals(parent.getIsActive())) {
            log.error("Parent topic with id: {} is inactive", parentId);
            throw new NotFoundException(ErrorCode.TOP_001);
        }
    }

    /**
     * Validate topic name is unique under same parent.
     */
    private void validateNameUnique(String name, String parentId) {
        boolean exists;
        if (parentId == null) {
            exists = topicRepository.existsByNameIgnoreCaseAndParentIdIsNull(name);
        } else {
            exists = topicRepository.existsByNameIgnoreCaseAndParentId(name, parentId);
        }

        if (exists) {
            log.error("Topic name '{}' already exists under parentId: {}", name, parentId);
            throw new DuplicateException(ErrorCode.TOP_003);
        }
    }

    /** Builds nested tree structure from flat list of topics. */
    private List <TopicTreeResponse> buildTopicsTree(List<Topic> topics) {
        log.info("Start build topic tree");
        if(topics.isEmpty()) {
            return Collections.emptyList();
        }

        var topicsByParentId = getTopicsByParentId(topics);
        var rootTopics =getRootTopics(topicsByParentId);

        return rootTopics.stream()
                .map(topic ->buildTreeRecursive(topic, topicsByParentId))
                .sorted(getTopicComparator())
                .collect(Collectors.toList());

    }

    /** Groups topics by parent ID. */
    private Map<String, List<Topic>> getTopicsByParentId(List<Topic> topics) {
        log.debug("Start getTopicsByParentId");
        return topics.stream()
                .collect(Collectors.groupingBy(
                        topic->topic.getParentId()==null ?"":topic.getParentId())
                );
    }

    /** Gets root topics (parentId is null). */
    private List<Topic> getRootTopics(Map<String, List<Topic>> topicsByParentId) {
        return topicsByParentId.getOrDefault("",Collections.emptyList());
    }

    /** Recursively builds tree for a topic and its children. */
    private TopicTreeResponse buildTreeRecursive(Topic topic, Map<String, List<Topic>> topicsByParentId) {
        var childTopics = topicsByParentId.getOrDefault(topic.getId(),Collections.emptyList());

        var children = childTopics.stream()
                .map(child->buildTreeRecursive(child, topicsByParentId))
                .sorted(getTopicComparator())
                .collect(Collectors.toList());

        return topicMapper.toTreeResponse(topic, children);
    }

    /** Creates comparator for sorting by order ASC, then name ASC. */
    private Comparator<TopicTreeResponse> getTopicComparator() {
        return Comparator
                .comparing(TopicTreeResponse::getOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TopicTreeResponse:: getName, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Retrieves a TopicResponse by its ID.
     *
     * @param id the ID of the topic
     * @return the TopicResponse corresponding to the given ID
     */
    @Override
    public TopicResponse getById(String id) {
        log.info("Get topic by id: {}", id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic not found with id: {}", id);
                    return new NotFoundException(TOP_001);
                });

        if (!Boolean.TRUE.equals(topic.getIsActive())) {
            log.error("Topic with id: {} is inactive", id);
            throw new NotFoundException(TOP_001);
        }

        log.info("Successfully retrieved topic with id: {}", id);
        return toResponse(topic);
    }

    /** Updates an existing topic with validation. */
    @CacheEvict(value = {"topics:all", "topics:tree"}, allEntries = true)
    @Override
    @Transactional
    public TopicResponse update(String id, TopicRequest request) {
        log.info("Start update topic with id: {}", id);

        // 1. Find topic by id
        var topic = findTopicById(id);

        // 2. Validate parent if provided
        if (request.getParentId() != null) {
            log.debug("ParentId provided: {}, validating...", request.getParentId());
            validateParentTopic(request.getParentId());

            // 3. Check circular reference if parentId changed
            if (!request.getParentId().equals(topic.getParentId())) {
                log.debug("ParentId changed from {} to {}, checking circular reference",
                        topic.getParentId(), request.getParentId());
                checkCircularReference(id, request.getParentId());
            }
        }

        // 4. Check name unique under same parent (exclude self)
        checkNameDuplicate(request.getName(), request.getParentId(), id);

        // 5. Update entity fields
        updateTopicFields(topic, request);

        // 6. Save to DB
        var savedTopic = topicRepository.save(topic);
        log.info("Topic updated successfully with id: {}", savedTopic.getId());

        return topicMapper.toResponse(savedTopic);
    }

    /** Finds topic by ID or throws NotFoundException. */
    private Topic findTopicById(String id) {
        return topicRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("Topic not found with id: {}", id);
                    return new NotFoundException(ErrorCode.TOP_001);
                } );
    }

    /** Validates that parent topic exists and is active. */
    private void validateParentTopic(String parentId) {
        var parent = topicRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("Parent topic not found with id: {}", parentId);
                    return new NotFoundException(ErrorCode.TOP_001);
                });

        if (!parent.getIsActive()) {
            log.error("Topic with id: {} is inactive", parentId);
            throw new NotFoundException(ErrorCode.TOP_001);
        }
    }

    /** Checks for circular reference in topic hierarchy. */
    private void checkCircularReference(String topicId, String newParentId) {
        if (newParentId == null) {
            log.debug("NewParentId is null, skip circular check");
            return;
        }
        if (newParentId.equals(topicId)) {
            log.error("Topic with id: {} is a circular reference", topicId);
            throw new BadRequestException(ErrorCode.TOP_002);
        }

        String currentId = newParentId;
        while (currentId != null) {
            log.debug("Checking parentId={}", currentId);
            var parent = topicRepository.findById(currentId).orElse(null);
            if (parent == null) {
                log.debug("Parent {} not found, stop traversal", currentId);
                break;
            }
            if (parent.getParentId() != null && parent.getParentId().equals(topicId)) {
                log.error("Circular reference detected: topic {} would create a cycle through {}", topicId, currentId);
                throw new BadRequestException(ErrorCode.TOP_002);
            }
            currentId = parent.getParentId();
        }
    }

    /** Checks if name is duplicate under same parent (exclude self). */
    private void checkNameDuplicate(String name, String parentId, String excludeId) {
        boolean exists;
        if (parentId == null) {
            exists = topicRepository.existsByNameAndParentIdIsNullAndIdNot(name, excludeId);
        } else {
            exists = topicRepository.existsByNameAndParentIdAndIdNot(name, parentId, excludeId);
        }
        if (exists) {
            log.error("Topic name '{}' already exists under parentId: {}", name, parentId);
            throw new DuplicateException(ErrorCode.TOP_003);
        }
    }

    /** Updates topic entity fields from request. */
    private void updateTopicFields(Topic topic, TopicRequest request) {
        log.debug("Updating fields: name={} -> {}, description={} -> {}, parentId={} -> {}",
                topic.getName(), request.getName(),
                topic.getDescription(), request.getDescription(),
                topic.getParentId(), request.getParentId());
        topic.setName(request.getName());
        topic.setDescription(request.getDescription());
        topic.setParentId(request.getParentId());

        if (request.getOrder() != null) {
            log.debug("Updating order: {} -> {}", topic.getOrder(), request.getOrder());
            topic.setOrder(request.getOrder());
        }
        if (request.getIsActive() != null) {
            log.debug("Updating isActive: {} -> {}", topic.getIsActive(), request.getIsActive());
            topic.setIsActive(request.getIsActive());
        }
    }
}
