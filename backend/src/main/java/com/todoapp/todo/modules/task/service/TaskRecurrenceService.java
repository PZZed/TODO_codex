package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.task.domain.RecurrenceFrequency;
import com.todoapp.todo.modules.task.domain.RecurringTaskRuleEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.dto.RecurrenceResponse;
import com.todoapp.todo.modules.task.dto.RecurrenceUpsertRequest;
import com.todoapp.todo.modules.task.dto.TaskOccurrenceResponse;
import com.todoapp.todo.modules.task.repository.RecurringTaskRuleRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TaskRecurrenceService {

    private final TaskRepository taskRepository;
    private final RecurringTaskRuleRepository recurringTaskRuleRepository;

    public TaskRecurrenceService(TaskRepository taskRepository, RecurringTaskRuleRepository recurringTaskRuleRepository) {
        this.taskRepository = taskRepository;
        this.recurringTaskRuleRepository = recurringTaskRuleRepository;
    }

    @Transactional
    public RecurrenceResponse create(UUID taskId, RecurrenceUpsertRequest request) {
        TaskEntity task = getActiveTask(taskId);
        recurringTaskRuleRepository.findByTaskId(taskId).ifPresent(r -> {
            if (r.isActive()) throw new BusinessConflictException("Active recurrence already exists for this task");
        });

        validateRequest(request);
        RecurringTaskRuleEntity entity = new RecurringTaskRuleEntity();
        applyRequest(entity, task, request);
        return toResponse(recurringTaskRuleRepository.save(entity));
    }

    @Transactional
    public RecurrenceResponse update(UUID taskId, RecurrenceUpsertRequest request) {
        RecurringTaskRuleEntity entity = recurringTaskRuleRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurrence not found for task: " + taskId));
        validateRequest(request);
        applyRequest(entity, entity.getTask(), request);
        return toResponse(recurringTaskRuleRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public RecurrenceResponse get(UUID taskId) {
        RecurringTaskRuleEntity entity = recurringTaskRuleRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurrence not found for task: " + taskId));
        return toResponse(entity);
    }

    @Transactional
    public void disable(UUID taskId) {
        RecurringTaskRuleEntity entity = recurringTaskRuleRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurrence not found for task: " + taskId));
        entity.setActive(false);
        recurringTaskRuleRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<TaskOccurrenceResponse> occurrencesForDay(UUID userId, LocalDate date) {
        return occurrencesForRange(userId, date, date);
    }

    @Transactional(readOnly = true)
    public List<TaskOccurrenceResponse> occurrencesForRange(UUID userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessConflictException("to must be greater than or equal to from");
        }

        List<RecurringTaskRuleEntity> rules = recurringTaskRuleRepository.findByTaskCreatedByIdAndActiveTrue(userId);
        List<TaskOccurrenceResponse> occurrences = new ArrayList<>();

        for (RecurringTaskRuleEntity rule : rules) {
            TaskEntity task = rule.getTask();
            if (task.getDeletedAt() != null) {
                continue;
            }
            LocalDate start = from.isAfter(rule.getStartDate()) ? from : rule.getStartDate();
            LocalDate end = rule.getEndDate() != null && rule.getEndDate().isBefore(to) ? rule.getEndDate() : to;
            if (end.isBefore(start)) continue;

            for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
                if (matches(rule, current)) {
                    occurrences.add(new TaskOccurrenceResponse(task.getId(), task.getTitle(), current, "RECURRENCE"));
                }
            }
        }

        occurrences.sort(Comparator.comparing(TaskOccurrenceResponse::occurrenceDate));
        return occurrences;
    }

    private boolean matches(RecurringTaskRuleEntity rule, LocalDate date) {
        return switch (rule.getFrequency()) {
            case DAILY -> matchesDaily(rule, date);
            case WEEKLY -> matchesWeekly(rule, date);
            case MONTHLY -> matchesMonthly(rule, date);
            case YEARLY -> false;
        };
    }

    private boolean matchesDaily(RecurringTaskRuleEntity rule, LocalDate date) {
        long diff = ChronoUnit.DAYS.between(rule.getStartDate(), date);
        return diff >= 0 && diff % rule.getIntervalValue() == 0;
    }

    private boolean matchesWeekly(RecurringTaskRuleEntity rule, LocalDate date) {
        long diffWeeks = ChronoUnit.WEEKS.between(rule.getStartDate(), date);
        if (diffWeeks < 0 || diffWeeks % rule.getIntervalValue() != 0) return false;

        Set<DayOfWeek> days = parseDays(rule.getDaysOfWeek());
        return days.contains(date.getDayOfWeek());
    }

    private boolean matchesMonthly(RecurringTaskRuleEntity rule, LocalDate date) {
        long months = ChronoUnit.MONTHS.between(
                rule.getStartDate().withDayOfMonth(1),
                date.withDayOfMonth(1)
        );
        if (months < 0 || months % rule.getIntervalValue() != 0) return false;

        int expectedDay = rule.getDayOfMonth() != null ? rule.getDayOfMonth() : rule.getStartDate().getDayOfMonth();
        return date.getDayOfMonth() == expectedDay;
    }

    private void validateRequest(RecurrenceUpsertRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BusinessConflictException("endDate must be greater than or equal to startDate");
        }

        if (request.frequency() == RecurrenceFrequency.WEEKLY
                && (request.daysOfWeek() == null || request.daysOfWeek().isEmpty())) {
            throw new BusinessConflictException("daysOfWeek is mandatory for weekly recurrence");
        }

        if (request.frequency() == RecurrenceFrequency.MONTHLY && request.dayOfMonth() != null) {
            if (request.dayOfMonth() < 1 || request.dayOfMonth() > 31) {
                throw new BusinessConflictException("dayOfMonth must be between 1 and 31");
            }
        }
    }

    private void applyRequest(RecurringTaskRuleEntity entity, TaskEntity task, RecurrenceUpsertRequest request) {
        entity.setTask(task);
        entity.setFrequency(request.frequency());
        entity.setIntervalValue(request.intervalValue());
        entity.setDaysOfWeek(formatDays(request.daysOfWeek()));
        entity.setDayOfMonth(request.dayOfMonth());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setTimezone(request.timezone() == null ? "UTC" : request.timezone());
        entity.setActive(true);
    }

    private String formatDays(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return null;
        return days.stream().sorted().map(Enum::name).reduce((a, b) -> a + "," + b).orElse(null);
    }

    private Set<DayOfWeek> parseDays(String value) {
        if (value == null || value.isBlank()) return Set.of();
        Set<DayOfWeek> result = new HashSet<>();
        for (String token : value.split(",")) {
            result.add(DayOfWeek.valueOf(token.trim()));
        }
        return result;
    }

    private RecurrenceResponse toResponse(RecurringTaskRuleEntity entity) {
        return new RecurrenceResponse(
                entity.getId(),
                entity.getTask().getId(),
                entity.getFrequency(),
                entity.getIntervalValue(),
                parseDays(entity.getDaysOfWeek()),
                entity.getDayOfMonth(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getTimezone(),
                entity.isActive()
        );
    }

    private TaskEntity getActiveTask(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (task.getDeletedAt() != null) throw new ResourceNotFoundException("Task not found: " + taskId);
        return task;
    }
}
