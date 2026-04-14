package com.todoapp.todo.modules.task.api;

import com.todoapp.todo.modules.task.dto.ReminderCreateRequest;
import com.todoapp.todo.modules.task.dto.ReminderResponse;
import com.todoapp.todo.modules.task.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Operation(summary = "Create reminder for task")
    @PostMapping("/tasks/{taskId}/reminders")
    public ResponseEntity<ReminderResponse> createForTask(@PathVariable UUID taskId,
                                                          @Valid @RequestBody ReminderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.createForTask(taskId, request));
    }

    @Operation(summary = "Create reminder for assignment")
    @PostMapping("/assignments/{assignmentId}/reminders")
    public ResponseEntity<ReminderResponse> createForAssignment(@PathVariable UUID assignmentId,
                                                                @Valid @RequestBody ReminderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.createForAssignment(assignmentId, request));
    }

    @Operation(summary = "List reminders by user")
    @GetMapping("/reminders")
    public List<ReminderResponse> listByUser(@RequestParam UUID userId) {
        return reminderService.listByUser(userId);
    }

    @Operation(summary = "Cancel reminder")
    @DeleteMapping("/reminders/{reminderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID reminderId) {
        reminderService.cancel(reminderId);
    }

    @Operation(summary = "Manually trigger due reminder dispatch")
    @PostMapping("/reminders/dispatch")
    public ResponseEntity<Integer> dispatch() {
        return ResponseEntity.ok(reminderService.dispatchDueReminders());
    }
}
