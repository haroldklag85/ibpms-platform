package com.ibpms.core.project.controller;

import com.ibpms.core.project.dto.AssignTaskDTO;
import com.ibpms.core.project.dto.ProjectTaskExecutionDTO;
import com.ibpms.core.project.service.GanttExecutionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/execution/projects")
public class GanttExecutionController {

    private final GanttExecutionService ganttService;

    // A simple registry for SSE emitters connected to a projectId.
    // In production, consider Redis Pub/Sub for distributed SSE nodes.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public GanttExecutionController(GanttExecutionService ganttService) {
        this.ganttService = ganttService;
    }

    @GetMapping("/{id}/gantt-tree")
    public List<ProjectTaskExecutionDTO> getGanttTree(@PathVariable String id) {
        return ganttService.getGanttTree(id);
    }

    @PutMapping("/tasks/{taskId}/assign")
    public ResponseEntity<Void> assignTask(@PathVariable String taskId, @RequestBody AssignTaskDTO dto) {
        ganttService.assignTask(taskId, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/baseline")
    public ResponseEntity<String> createBaseline(@PathVariable String id) {
        String baselineId = ganttService.freezeBaseline(id);
        return ResponseEntity.ok(baselineId);
    }

    // ----------------------------------------------------
    // AC-3: Server-Sent Events (SSE) para actualizaciones.
    // ----------------------------------------------------
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGanttUpdates(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout

        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(id);
        });
        emitter.onError(e -> {
            emitter.completeWithError(e);
            emitters.remove(id);
        });

        return emitter;
    }

    /**
     * Helper method to be called from a Camunda ExecutionListener when a generic
     * task completes.
     */
    public void pushUpdateToFrontend(String projectId, String taskId, String newStatus) {
        SseEmitter emitter = emitters.get(projectId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("gantt-task-update")
                        .data("{\"taskId\": \"" + taskId + "\", \"status\": \"" + newStatus + "\"}"));
            } catch (IOException e) {
                emitters.remove(projectId);
            }
        }
    }
}
