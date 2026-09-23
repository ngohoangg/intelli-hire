package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.DifficultyLevelRequest;
import org.aibles.intellihirequestion.dto.DifficultyLevelResponse;
import org.aibles.intellihirequestion.service.DifficultyLevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/v1/difficulties")
@RestController
@RequiredArgsConstructor
@Slf4j
public class DifficultyController {

    private final DifficultyLevelService difficultyLevelService;

    /**
     * Get all difficulty levels
     *
     * @return list of difficulty levels
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<DifficultyLevelResponse>>> getAllDifficultyLevels() {
        log.info("Request to get all difficulty levels");
        List<DifficultyLevelResponse> responses = difficultyLevelService.getAll();
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    /**
     * Create a new difficulty level.
     *
     * @param request the difficulty level request payload
     * @return the created difficulty level
     */
    @PostMapping
    public ResponseEntity<BaseResponse<DifficultyLevelResponse>> createDifficultyLevel(@Valid @RequestBody DifficultyLevelRequest request) {
        log.info("Request to create difficulty level with name: {}", request.getName());
        DifficultyLevelResponse response = difficultyLevelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    /**
     * Get difficulty level by ID.
     *
     * @param id the difficulty level ID
     * @return the difficulty level details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<DifficultyLevelResponse>> getDifficultyLevelById(@PathVariable String id) {
        log.info("Request to get difficulty level by id: {}", id);
        DifficultyLevelResponse response = difficultyLevelService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Update an existing difficulty level.
     *
     * @param id the difficulty level ID
     * @param request the updated difficulty level request payload
     * @return the updated difficulty level details
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<DifficultyLevelResponse>> updateDifficultyLevel(
            @PathVariable String id,
            @Valid @RequestBody DifficultyLevelRequest request) {
        log.info("Request to update difficulty level with id: {}", id);
        DifficultyLevelResponse response = difficultyLevelService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * Delete a difficulty level by ID.
     *
     * @param id the difficulty level ID to delete
     * @return success response with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteDifficultyLevel(@PathVariable String id) {
        log.info("Request to delete difficulty level with id: {}", id);
        difficultyLevelService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
