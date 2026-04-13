package com.todoapp.todo.modules.task.api;

import com.todoapp.todo.modules.task.dto.TaskCreateRequest;
import com.todoapp.todo.modules.task.dto.TaskResponse;
import com.todoapp.todo.modules.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
