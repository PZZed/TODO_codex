package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.RecurrenceFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record RecurrenceUpsertRequest(
        @NotNull RecurrenceFrequency frequency,
        @NotNull @Min(1) Integer intervalValue,
        Set<DayOfWeek> daysOfWeek,
        Integer dayOfMonth,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @Size(min = 2, max = 64) String timezone
) {
}
