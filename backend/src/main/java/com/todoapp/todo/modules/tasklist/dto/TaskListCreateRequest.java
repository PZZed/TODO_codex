package com.todoapp.todo.modules.tasklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TaskListCreateRequest(
        @NotNull UUID ownerUserId,
        @NotBlank @Size(min = 1, max = 120) String name,
        String color
) {}
