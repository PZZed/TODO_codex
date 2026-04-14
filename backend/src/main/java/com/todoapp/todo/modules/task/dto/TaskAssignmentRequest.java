package com.todoapp.todo.modules.task.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record TaskAssignmentRequest(
        @NotNull LocalDate assignmentDate,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        String note
) {
}
