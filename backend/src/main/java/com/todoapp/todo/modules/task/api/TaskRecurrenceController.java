package com.todoapp.todo.modules.task.api;

import com.todoapp.todo.modules.task.dto.RecurrenceResponse;
import com.todoapp.todo.modules.task.dto.RecurrenceUpsertRequest;
import com.todoapp.todo.modules.task.dto.TaskOccurrenceResponse;
import com.todoapp.todo.modules.task.service.TaskRecurrenceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TaskRecurrenceController {

    private final TaskRecurrenceService taskRecurrenceService;

    public TaskRecurrenceController(TaskRecurrenceService taskRecurrenceService) {
        this.taskRecurrenceService = taskRecurrenceService;
    }

    @Operation(summary = "Create recurrence rule")
    @PostMapping("/tasks/{taskId}/recurrence")
    public ResponseEntity<RecurrenceResponse> create(@PathVariable UUID taskId,
                                                     @Valid @RequestBody RecurrenceUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRecurrenceService.create(taskId, request));
    }

    @Operation(summary = "Update recurrence rule")
    @PatchMapping("/tasks/{taskId}/recurrence")
    public RecurrenceResponse update(@PathVariable UUID taskId,
                                     @Valid @RequestBody RecurrenceUpsertRequest request) {
        return taskRecurrenceService.update(taskId, request);
    }

    @Operation(summary = "Get recurrence rule")
    @GetMapping("/tasks/{taskId}/recurrence")
    public RecurrenceResponse get(@PathVariable UUID taskId) {
        return taskRecurrenceService.get(taskId);
    }

    @Operation(summary = "Disable recurrence rule")
    @DeleteMapping("/tasks/{taskId}/recurrence")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID taskId) {
        taskRecurrenceService.disable(taskId);
    }

    @Operation(summary = "Get recurrence occurrences for one day")
    @GetMapping("/recurrences/day")
    public List<TaskOccurrenceResponse> day(@RequestParam UUID userId,
                                            @RequestParam LocalDate date) {
        return taskRecurrenceService.occurrencesForDay(userId, date);
    }

    @Operation(summary = "Get recurrence occurrences for a period")
    @GetMapping("/recurrences/range")
    public List<TaskOccurrenceResponse> range(@RequestParam UUID userId,
                                              @RequestParam LocalDate from,
                                              @RequestParam LocalDate to) {
        return taskRecurrenceService.occurrencesForRange(userId, from, to);
    }
}
