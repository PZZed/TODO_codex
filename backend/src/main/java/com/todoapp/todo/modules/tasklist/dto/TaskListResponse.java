package com.todoapp.todo.modules.tasklist.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskListResponse(
        UUID id,
        UUID ownerUserId,
        String name,
        String color,
        boolean archived,
        Integer position,
        Instant createdAt,
        Instant updatedAt
) {}
