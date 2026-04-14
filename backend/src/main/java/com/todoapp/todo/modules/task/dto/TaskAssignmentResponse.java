package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.DailyAssignmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TaskAssignmentResponse(
        UUID id,
        UUID taskId,
        UUID userId,
        LocalDate assignmentDate,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        DailyAssignmentStatus statusOnDay,
        String note
) {
}
