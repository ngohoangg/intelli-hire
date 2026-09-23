package org.aibles.intellihirequestion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.RubricRequest;
import org.aibles.intellihirequestion.dto.RubricResponse;
import org.aibles.intellihirequestion.entity.Rubric;
import org.aibles.intellihirequestion.exception.DuplicateException;
import org.aibles.intellihirequestion.exception.ErrorCode;
import org.aibles.intellihirequestion.exception.NotFoundException;
import org.aibles.intellihirequestion.mapper.RubricMapper;
import org.aibles.intellihirequestion.repository.QuestionRepository;
import org.aibles.intellihirequestion.repository.RubricRepository;
import org.aibles.intellihirequestion.service.RubricService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RubricServiceImpl implements RubricService {

    private final QuestionRepository questionRepository;
    private final RubricRepository rubricRepository;
    private final RubricMapper rubricMapper;

    @Override
    @Transactional
    public RubricResponse createRubric(String questionId, RubricRequest request) {
        log.info("createRubric - questionId: {}", questionId);

        if (!questionRepository.existsById(questionId)) {
            log.error("Question not found with id: {}", questionId);
            throw new NotFoundException(ErrorCode.QUE_001);
        }

        if (rubricRepository.existsByQuestionId(questionId)) {
            log.error("Rubric already exists for question: {}", questionId);
            throw new DuplicateException(ErrorCode.RUB_002);
        }

        Rubric rubric = rubricMapper.toEntity(questionId, request);
        Rubric saved;
        try {
            saved = rubricRepository.save(rubric);
        } catch (DataIntegrityViolationException e) {
            log.error("Rubric already exists for question (concurrent): {}", questionId);
            throw new DuplicateException(ErrorCode.RUB_002);
        }

        log.info("createRubric - Successfully created rubric {} for question {}", saved.getId(), questionId);
        return rubricMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RubricResponse getRubric(String questionId) {
        log.info("getRubric - questionId: {}", questionId);

        if (!questionRepository.existsById(questionId)) {
            log.error("Question not found with id: {}", questionId);
            throw new NotFoundException(ErrorCode.QUE_001);
        }

        Rubric rubric = rubricRepository.findByQuestionId(questionId)
                .orElseThrow(() -> {
                    log.error("Rubric not found for question: {}", questionId);
                    return new NotFoundException(ErrorCode.RUB_001);
                });

        return rubricMapper.toResponse(rubric);
    }

    @Override
    @Transactional
    public RubricResponse updateRubric(String questionId, RubricRequest request) {
        log.info("updateRubric - questionId: {}", questionId);

        if (!questionRepository.existsById(questionId)) {
            log.error("Question not found with id: {}", questionId);
            throw new NotFoundException(ErrorCode.QUE_001);
        }

        Rubric rubric = rubricRepository.findByQuestionId(questionId)
                .orElseThrow(() -> {
                    log.error("Rubric not found for question: {}", questionId);
                    return new NotFoundException(ErrorCode.RUB_001);
                });

        rubricMapper.updateEntity(rubric, request);
        Rubric saved = rubricRepository.save(rubric);

        log.info("updateRubric - Successfully updated rubric {} for question {}", saved.getId(), questionId);
        return rubricMapper.toResponse(saved);
    }
}
