package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.ReminderChannel;
import com.todoapp.todo.modules.task.domain.ReminderTriggerMode;
import com.todoapp.todo.modules.task.domain.ReminderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReminderCreateRequest(
        @NotNull ReminderType type,
        @NotNull ReminderTriggerMode triggerMode,
        @Min(1) Integer minutesBeforeDue,
        Instant triggerAt,
        ReminderChannel channel
) {
}
