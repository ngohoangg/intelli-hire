package org.aibles.intellihirequestion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.TagRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.entity.Tag;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.TagMapper;
import org.aibles.intellihirequestion.repository.QuestionTagRepository;
import org.aibles.intellihirequestion.repository.TagRepository;
import org.aibles.intellihirequestion.service.TagService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final QuestionTagRepository questionTagRepository;

    /**
     * Get all tags ordered by name ascending.
     * Results are cached with key "tags:all" and TTL = 1 hour.
     *
     * @return list of TagResponse
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'all'")
    public List<TagResponse> getAll() {
        log.info("Cache miss for getAll tags, fetching from database.");
        var tags = tagRepository.findAllByOrderByNameAsc();
        return tagMapper.toResponseList(tags);
    }

    /**
     * Create a new tag.
     */
    @Override
    @Transactional
    @CacheEvict(value = "tags", key = "'all'")
    public TagResponse create(TagRequest request) {
        log.info("Creating new tag with name: {}", request.getName());

        String normalizedName = normalizeName(request.getName());

        validateNameUnique(normalizedName);

        Tag tag = tagMapper.toEntity(request);
        Tag savedTag = tagRepository.save(tag);

        log.info("Created tag with id: {}", savedTag.getId());
        return TagMapper.toResponse(savedTag);
    }

    /**
     * Update an existing tag by ID.
     *
     * @param id      the tag ID
     * @param request the tag request
     * @return the updated tag response
     */
    @Transactional
    @CacheEvict(value = "tags", key = "'all'")
    @Override
    public TagResponse update(String id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Tag with id '{}' not found for update", id);
                    return new NotFoundException(ErrorCode.TAG_001);
                });

        log.info("Updating tag with id: {}", id);
        String normalizedName = normalizeName(request.getName()); // Normalize the name

        if (!tag.getName().equalsIgnoreCase(normalizedName)) {
            if (tagRepository.existsByNameAndIdNot(normalizedName, id)) {
                log.error("Tag name '{}' already exists", normalizedName);
                throw new DuplicateException(ErrorCode.TAG_002);
            }
        }

        tag.setName(normalizedName);
        tag.setLastUpdatedAt(LocalDateTime.now());
        Tag updatedTag = tagRepository.save(tag);
        log.info("Updated tag with id: {}", updatedTag.getId());
        return TagMapper.toResponse(updatedTag);
    }


    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }


    private void validateNameUnique(String name) {
        if (tagRepository.existsByName(name)) {
            log.error("Tag name '{}' already exists", name);
            throw new DuplicateException(ErrorCode.TAG_002);
        }
    }

    /**
     * Delete a tag by ID.
     * Validates that tag is not being used by any questions before deletion.
     *
     * @param id the tag ID to delete
     */
    @Override
    @Transactional
    @CacheEvict(value = "tags", key = "'all'")
    public void delete(String id) {
        log.info("Deleting tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Tag not found with id: {}", id);
                    return new NotFoundException(ErrorCode.TAG_001);
                });

        if (questionTagRepository.existsByTagId(id)) {
            log.error("Tag with id: {} is in use by questions", id);
            throw new BadRequestException(ErrorCode.TAG_003);
        }

        tagRepository.delete(tag);
        log.info("Deleted tag with id: {}", id);
    }
}
