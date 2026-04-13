package com.todoapp.todo.modules.tasklist.api;

import com.todoapp.todo.modules.tasklist.dto.TaskListCreateRequest;
import com.todoapp.todo.modules.tasklist.dto.TaskListResponse;
import com.todoapp.todo.modules.tasklist.service.TaskListService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task-lists")
public class TaskListController {

    private final TaskListService taskListService;

    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @Operation(summary = "Create task list")
    @PostMapping
    public ResponseEntity<TaskListResponse> create(@Valid @RequestBody TaskListCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskListService.create(request));
    }

    @Operation(summary = "List task lists by owner")
    @GetMapping
    public List<TaskListResponse> findByOwner(@RequestParam UUID ownerUserId) {
        return taskListService.findByOwner(ownerUserId);
    }
}
