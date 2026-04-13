package com.todoapp.todo.modules.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String timezone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
