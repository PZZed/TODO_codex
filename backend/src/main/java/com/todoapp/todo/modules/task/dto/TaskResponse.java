package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.TaskPriority;
import com.todoapp.todo.modules.task.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID taskListId,
        UUID createdByUserId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant startAt,
        Instant dueAt,
        Instant completedAt,
        Instant deletedAt,
        boolean allDay,
        Instant createdAt,
        Instant updatedAt
) {}
