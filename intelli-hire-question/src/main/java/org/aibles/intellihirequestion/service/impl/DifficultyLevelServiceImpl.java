package org.aibles.intellihirequestion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.DifficultyLevelRequest;
import org.aibles.intellihirequestion.dto.DifficultyLevelResponse;
import org.aibles.intellihirequestion.entity.DifficultyLevel;
import org.aibles.intellihirequestion.exception.BadRequestException;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.DifficultyLevelMapper;
import org.aibles.intellihirequestion.repository.DifficultyLevelRepository;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.StudyArticleRepository;
import org.aibles.intellihirequestion.service.DifficultyLevelService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DifficultyLevelServiceImpl implements DifficultyLevelService {

    private final DifficultyLevelRepository difficultyLevelRepository;
    private final QuestionRepository questionRepository;
    private final StudyArticleRepository studyArticleRepository;
    private final DifficultyLevelMapper difficultyLevelMapper;

    /**
     * Retrieves all difficulty levels.
     *
     * @return a list of DifficultyLevelResponse objects
     */
    @Cacheable(value = "difficulties", key = "'all'")
    @Override
    @Transactional(readOnly = true)
    public List<DifficultyLevelResponse> getAll() {
        log.info("CACHE MISS - Fetching difficulty levels from database");
        var difficulties = difficultyLevelRepository.findAllByOrderByWeightAsc();
        return difficultyLevelMapper.toResponseList(difficulties);
    }

    /**
     * Creates a new difficulty level.
     *
     * @param request the difficulty level request payload
     * @return the created difficulty level response
     */
    @Override
    @Transactional
    @CacheEvict(value = "difficulties", key = "'all'")
    public DifficultyLevelResponse create(DifficultyLevelRequest request) {
        String normalizedName = request.getName().trim();
        log.info("Creating difficulty level with name: {}", normalizedName);

        if (difficultyLevelRepository.existsByNameIgnoreCase(normalizedName)) {
            log.error("Difficulty level already exists with name: {}", normalizedName);
            throw new DuplicateException(ErrorCode.DIF_002);
        }

        var entity = difficultyLevelMapper.toEntity(request);
        entity.setName(normalizedName);
        var saved = difficultyLevelRepository.save(entity);
        log.info("Created difficulty level with id: {}", saved.getId());
        return difficultyLevelMapper.toResponse(saved);
    }

    /**
     * Retrieves a difficulty level by ID.
     *
     * @param id the difficulty level ID
     * @return the corresponding DifficultyLevelResponse
     */
    @Override
    @Transactional(readOnly = true)
    public DifficultyLevelResponse getById(String id) {
        log.info("CACHE MISS - Fetching difficulty level by id: {}", id);
        var difficulty = difficultyLevelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Difficulty level not found with ID: {}", id);
                    return new NotFoundException(ErrorCode.DIF_001);
                });
        return difficultyLevelMapper.toResponse(difficulty);
    }

    /**
     * Updates an existing difficulty level.
     *
     * @param id the difficulty level ID
     * @param request the updated difficulty level request payload
     * @return the updated DifficultyLevelResponse
     */
    @Override
    @Transactional
    @CacheEvict(value = "difficulties", key = "'all'")
    public DifficultyLevelResponse update(String id, DifficultyLevelRequest request) {
        log.info("Updating difficulty level with id: {}", id);

        String normalizedName = request.getName().trim();
        DifficultyLevel difficultyLevel = difficultyLevelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Difficulty level not found with ID: {}", id);
                    return new NotFoundException(ErrorCode.DIF_001);
                });

        if (!difficultyLevel.getName().equalsIgnoreCase(normalizedName) && difficultyLevelRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            log.error("Difficulty level already exists with name: {}", normalizedName);
            throw new DuplicateException(ErrorCode.DIF_002);
        }

        difficultyLevel.setName(normalizedName);
        difficultyLevel.setWeight(request.getWeight());
        difficultyLevel.setDescription(request.getDescription());

        DifficultyLevel updatedDifficultyLevel = difficultyLevelRepository.save(difficultyLevel);
        return difficultyLevelMapper.toResponse(updatedDifficultyLevel);
    }

    /**
     * Deletes a difficulty level by ID.
     *
     * @param id the difficulty level ID to delete
     */
    @Override
    @Transactional
    @CacheEvict(value = "difficulties", key = "'all'")
    public void delete(String id) {
        log.info("Deleting difficulty level with id: {}", id);

        DifficultyLevel difficultyLevel = difficultyLevelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Difficulty level not found with ID: {}", id);
                    return new NotFoundException(ErrorCode.DIF_001);
                });

        if (questionRepository.existsByDifficultyId(id) || studyArticleRepository.existsByDifficultyId(id)) {
            log.error("Cannot delete difficulty level with id: {} because it is associated with questions or study articles", id);
            throw new BadRequestException(ErrorCode.DIF_003);
        }

        difficultyLevelRepository.delete(difficultyLevel);
        log.info("Deleted difficulty level with id: {}", id);
    }
}
