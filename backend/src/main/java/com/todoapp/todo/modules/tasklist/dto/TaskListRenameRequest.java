package com.todoapp.todo.modules.tasklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskListRenameRequest(
        @NotBlank @Size(min = 1, max = 120) String name
) {
}
