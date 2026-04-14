package com.todoapp.todo.modules.task.dto;

import com.todoapp.todo.modules.task.domain.ReminderChannel;
import com.todoapp.todo.modules.task.domain.ReminderStatus;
import com.todoapp.todo.modules.task.domain.ReminderTriggerMode;
import com.todoapp.todo.modules.task.domain.ReminderType;

import java.time.Instant;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        UUID taskId,
        UUID dailyAssignmentId,
        UUID userId,
        ReminderType type,
        ReminderTriggerMode triggerMode,
        Integer minutesBeforeDue,
        Instant triggerAt,
        ReminderChannel channel,
        ReminderStatus status,
        Integer attemptCount,
        Instant lastAttemptAt
) {
}
