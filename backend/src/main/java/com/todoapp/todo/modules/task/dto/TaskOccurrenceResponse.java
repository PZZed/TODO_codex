package com.todoapp.todo.modules.task.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TaskOccurrenceResponse(
        UUID taskId,
        String title,
        LocalDate occurrenceDate,
        String source
) {
}
