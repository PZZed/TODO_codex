package com.todoapp.todo.modules.task.api;

import com.todoapp.todo.modules.task.dto.*;
import com.todoapp.todo.modules.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create task")
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @Operation(summary = "Update task")
    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    @Operation(summary = "Mark task as completed")
    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable UUID id) {
        return taskService.markCompleted(id);
    }

    @Operation(summary = "Assign task to date")
    @PostMapping("/{id}/assignments")
    public ResponseEntity<TaskAssignmentResponse> assign(@PathVariable UUID id, @Valid @RequestBody TaskAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.assignToDate(id, request));
    }

    @Operation(summary = "Delete task")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

    @Operation(summary = "Get task by id")
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable UUID id) {
        return taskService.findById(id);
    }

    @Operation(summary = "List tasks by task list")
    @GetMapping
    public List<TaskResponse> findByTaskList(@RequestParam UUID taskListId) {
        return taskService.findByTaskList(taskListId);
    }

    @Operation(summary = "Get tasks for a given day")
    @GetMapping("/day")
    public TaskDayResponse findForDay(@RequestParam UUID userId,
                                      @RequestParam LocalDate date,
                                      @RequestParam(defaultValue = "UTC") String timezone) {
        return taskService.findTasksForDay(userId, date, ZoneId.of(timezone));
    }
}
