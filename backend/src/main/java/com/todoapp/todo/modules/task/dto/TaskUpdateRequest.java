package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.TaskPriority;
import com.todoapp.todo.modules.task.domain.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record TaskUpdateRequest(
        @Size(min = 1, max = 255) String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant startAt,
        Instant dueAt,
        Boolean allDay
) {
}
