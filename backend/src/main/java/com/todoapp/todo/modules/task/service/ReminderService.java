package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.task.domain.*;
import com.todoapp.todo.modules.task.dto.ReminderCreateRequest;
import com.todoapp.todo.modules.task.dto.ReminderResponse;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.ReminderRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final DailyTaskAssignmentRepository assignmentRepository;

    public ReminderService(ReminderRepository reminderRepository,
                          TaskRepository taskRepository,
                          DailyTaskAssignmentRepository assignmentRepository) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public ReminderResponse createForTask(UUID taskId, ReminderCreateRequest request) {
        TaskEntity task = getActiveTask(taskId);
        validateTriggerRequest(task.getDueAt(), request);

        ReminderEntity entity = new ReminderEntity();
        entity.setTask(task);
        entity.setUser(task.getCreatedBy());
        applyReminderRequest(entity, request, task.getDueAt());

        return toResponse(reminderRepository.save(entity));
    }

    @Transactional
    public ReminderResponse createForAssignment(UUID assignmentId, ReminderCreateRequest request) {
        DailyTaskAssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        Instant assignmentBase = computeAssignmentBase(assignment);
        validateTriggerRequest(assignmentBase, request);

        ReminderEntity entity = new ReminderEntity();
        entity.setTask(assignment.getTask());
        entity.setDailyAssignment(assignment);
        entity.setUser(assignment.getUser());
        applyReminderRequest(entity, request, assignmentBase);

        return toResponse(reminderRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> listByUser(UUID userId) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancel(UUID reminderId) {
        ReminderEntity reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + reminderId));
        if (reminder.getStatus() == ReminderStatus.SENT) {
            throw new BusinessConflictException("Cannot cancel a sent reminder");
        }
        reminder.setStatus(ReminderStatus.CANCELED);
        reminderRepository.save(reminder);
    }

    @Transactional
    public int dispatchDueReminders() {
        List<ReminderEntity> due = reminderRepository.findByStatusAndTriggerAtLessThanEqual(ReminderStatus.SCHEDULED, Instant.now());
        for (ReminderEntity reminder : due) {
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setAttemptCount(reminder.getAttemptCount() + 1);
            reminder.setLastAttemptAt(Instant.now());
            reminderRepository.save(reminder);
        }
        return due.size();
    }

    private TaskEntity getActiveTask(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (task.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private void validateTriggerRequest(Instant baseDate, ReminderCreateRequest request) {
        if (request.triggerMode() == ReminderTriggerMode.RELATIVE_DUE) {
            if (request.minutesBeforeDue() == null) {
                throw new BusinessConflictException("minutesBeforeDue is required for RELATIVE_DUE mode");
            }
            if (baseDate == null) {
                throw new BusinessConflictException("Base date is required for RELATIVE_DUE mode");
            }
        } else {
            if (request.triggerAt() == null) {
                throw new BusinessConflictException("triggerAt is required for ABSOLUTE_DATETIME mode");
            }
        }
    }

    private void applyReminderRequest(ReminderEntity entity, ReminderCreateRequest request, Instant baseDate) {
        entity.setType(request.type());
        entity.setTriggerMode(request.triggerMode());
        entity.setMinutesBeforeDue(request.minutesBeforeDue());
        entity.setChannel(request.channel() == null ? ReminderChannel.IN_APP : request.channel());

        if (request.triggerMode() == ReminderTriggerMode.RELATIVE_DUE) {
            entity.setTriggerAt(baseDate.minus(Duration.ofMinutes(request.minutesBeforeDue())));
        } else {
            entity.setTriggerAt(request.triggerAt());
        }
        entity.setStatus(ReminderStatus.SCHEDULED);
    }

    private Instant computeAssignmentBase(DailyTaskAssignmentEntity assignment) {
        String timezone = assignment.getUser().getTimezone() == null ? "UTC" : assignment.getUser().getTimezone();
        ZoneId zoneId = ZoneId.of(timezone);
        LocalTime time = assignment.getPlannedStartTime() == null ? LocalTime.of(9, 0) : assignment.getPlannedStartTime();
        return assignment.getAssignmentDate().atTime(time).atZone(zoneId).toInstant();
    }

    private ReminderResponse toResponse(ReminderEntity entity) {
        return new ReminderResponse(
                entity.getId(),
                entity.getTask().getId(),
                entity.getDailyAssignment() == null ? null : entity.getDailyAssignment().getId(),
                entity.getUser().getId(),
                entity.getType(),
                entity.getTriggerMode(),
                entity.getMinutesBeforeDue(),
                entity.getTriggerAt(),
                entity.getChannel(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getLastAttemptAt()
        );
    }
}
