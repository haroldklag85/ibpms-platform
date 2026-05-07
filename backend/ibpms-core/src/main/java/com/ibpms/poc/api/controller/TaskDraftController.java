// @Traceability: US-017 - CA-07, CA-14
package com.ibpms.poc.api.controller;

import com.ibpms.poc.domain.model.TaskDraft;
import com.ibpms.poc.domain.port.TaskDraftRepository;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workbox/tasks/{taskId}/draft")
public class TaskDraftController {

    private final TaskDraftRepository taskDraftRepository;
    private final Bucket draftRateLimiterBucket;

    public TaskDraftController(TaskDraftRepository taskDraftRepository, Bucket draftRateLimiterBucket) {
        this.taskDraftRepository = taskDraftRepository;
        this.draftRateLimiterBucket = draftRateLimiterBucket;
    }

    @GetMapping
    public ResponseEntity<?> getDraft(@PathVariable String taskId, Authentication authentication) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        
        String userId = authentication.getName();
        Optional<TaskDraft> draft = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        return draft.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> saveOrUpdateDraft(
            @PathVariable String taskId,
            @RequestBody DraftRequest request,
            Authentication authentication) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        String userId = authentication.getName();
        Optional<TaskDraft> existing = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);

        TaskDraft draft = existing.orElseGet(() -> TaskDraft.builder()
                .id(UUID.randomUUID())
                .taskId(taskId)
                .userId(userId)
                .createdAt(ZonedDateTime.now())
                .build());

        draft.setCurrentStep(request.currentStep());
        draft.setPartialData(request.partialData());
        draft.setSchemaVersion(request.schemaVersion());
        draft.setUpdatedAt(ZonedDateTime.now());

        TaskDraft saved = taskDraftRepository.save(draft);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteDraft(@PathVariable String taskId, Authentication authentication) {
        if (!draftRateLimiterBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "10").build();
        }
        String userId = authentication.getName();
        Optional<TaskDraft> existing = taskDraftRepository.findByTaskIdAndUserId(taskId, userId);
        existing.ifPresent(taskDraft -> taskDraftRepository.deleteById(taskDraft.getId()));
        return ResponseEntity.noContent().build();
    }
}

record DraftRequest(Integer currentStep, String partialData, String schemaVersion) {}
