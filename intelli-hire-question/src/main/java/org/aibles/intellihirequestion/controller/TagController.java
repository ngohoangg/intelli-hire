package org.aibles.intellihirequestion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihirequestion.dto.BaseResponse;
import org.aibles.intellihirequestion.dto.TagRequest;
import org.aibles.intellihirequestion.dto.TagResponse;
import org.aibles.intellihirequestion.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    /**
     * Get all tags for dropdown/autocomplete selection.
     *
     * @return list of tags ordered by name ASC
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<TagResponse>>> getAllTags() {
        log.info("Request to get all tags");
        List<TagResponse> responses = tagService.getAll();
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    /**
     * Create a new tag.
     *
     * @param request the tag request
     * @return the created tag response
     */
    @PostMapping
    public ResponseEntity<BaseResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest request) {
        log.info("Request to create tag with name: {}", request.getName());
        TagResponse response = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    /**
     * Delete a tag by ID.
     *
     * @param id the tag ID to delete
     * @return empty response on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteTag(@PathVariable String id) {
        log.info("DELETE /v1/tags/{}", id);
        tagService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }

    /**
     * Update an existing tag by ID.
     *
     * @param id      the tag ID
     * @param request the tag request
     * @return the updated tag response
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<TagResponse>> updateTag(@PathVariable String id,
                                                               @Valid @RequestBody TagRequest request) {
        log.info("Request to update tag with id: {}", id);
        TagResponse response = tagService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
