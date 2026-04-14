package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.modules.task.domain.RecurrenceFrequency;
import com.todoapp.todo.modules.task.domain.RecurringTaskRuleEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.dto.RecurrenceUpsertRequest;
import com.todoapp.todo.modules.task.dto.TaskOccurrenceResponse;
import com.todoapp.todo.modules.task.repository.RecurringTaskRuleRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskRecurrenceServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RecurringTaskRuleRepository recurringTaskRuleRepository;

    @InjectMocks
    private TaskRecurrenceService taskRecurrenceService;

    @Test
    void create_shouldFail_whenWeeklyWithoutDays() {
        UUID taskId = UUID.randomUUID();
        TaskEntity task = new TaskEntity();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(recurringTaskRuleRepository.findByTaskId(taskId)).thenReturn(Optional.empty());

        RecurrenceUpsertRequest request = new RecurrenceUpsertRequest(
                RecurrenceFrequency.WEEKLY,
                1,
                Set.of(),
                null,
                LocalDate.of(2026, 4, 1),
                null,
                "UTC"
        );

        assertThatThrownBy(() -> taskRecurrenceService.create(taskId, request))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void occurrencesForDay_shouldReturnDailyOccurrence() {
        UUID userId = UUID.randomUUID();
        TaskEntity task = new TaskEntity();
        task.setTitle("Réviser");

        UserEntity user = new UserEntity();
        task.setCreatedBy(user);

        RecurringTaskRuleEntity rule = new RecurringTaskRuleEntity();
        rule.setTask(task);
        rule.setFrequency(RecurrenceFrequency.DAILY);
        rule.setIntervalValue(1);
        rule.setStartDate(LocalDate.of(2026, 4, 1));
        rule.setEndDate(LocalDate.of(2026, 4, 30));
        rule.setActive(true);

        when(recurringTaskRuleRepository.findByTaskCreatedByIdAndActiveTrue(userId)).thenReturn(List.of(rule));

        List<TaskOccurrenceResponse> occurrences = taskRecurrenceService.occurrencesForDay(userId, LocalDate.of(2026, 4, 15));

        assertThat(occurrences).hasSize(1);
        assertThat(occurrences.get(0).occurrenceDate()).isEqualTo(LocalDate.of(2026, 4, 15));
    }

    @Test
    void occurrencesForRange_shouldHandleWeeklySpecificDays() {
        UUID userId = UUID.randomUUID();
        TaskEntity task = new TaskEntity();
        task.setTitle("Sport");

        RecurringTaskRuleEntity rule = new RecurringTaskRuleEntity();
        rule.setTask(task);
        rule.setFrequency(RecurrenceFrequency.WEEKLY);
        rule.setIntervalValue(1);
        rule.setDaysOfWeek(DayOfWeek.MONDAY.name() + "," + DayOfWeek.WEDNESDAY.name());
        rule.setStartDate(LocalDate.of(2026, 4, 1));
        rule.setActive(true);

        when(recurringTaskRuleRepository.findByTaskCreatedByIdAndActiveTrue(userId)).thenReturn(List.of(rule));

        List<TaskOccurrenceResponse> occurrences = taskRecurrenceService.occurrencesForRange(
                userId,
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 4, 19)
        );

        assertThat(occurrences).hasSize(2);
        assertThat(occurrences).extracting(TaskOccurrenceResponse::occurrenceDate)
                .containsExactly(LocalDate.of(2026, 4, 13), LocalDate.of(2026, 4, 15));
    }

    @Test
    void occurrencesForRange_shouldFail_whenInvalidRange() {
        assertThatThrownBy(() -> taskRecurrenceService.occurrencesForRange(
                UUID.randomUUID(),
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 19)
        )).isInstanceOf(BusinessConflictException.class);
    }
}
