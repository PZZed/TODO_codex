package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.RecurrenceFrequency;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record RecurrenceResponse(
        UUID id,
        UUID taskId,
        RecurrenceFrequency frequency,
        Integer intervalValue,
        Set<DayOfWeek> daysOfWeek,
        Integer dayOfMonth,
        LocalDate startDate,
        LocalDate endDate,
        String timezone,
        boolean active
) {
}
