package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.modules.task.domain.*;
import com.todoapp.todo.modules.task.dto.ReminderCreateRequest;
import com.todoapp.todo.modules.task.dto.ReminderResponse;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.ReminderRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DailyTaskAssignmentRepository assignmentRepository;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void createForTask_shouldComputeRelativeTriggerAt() {
        UUID taskId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        TaskEntity task = new TaskEntity();
        task.setCreatedBy(user);
        task.setDueAt(Instant.parse("2026-04-20T10:00:00Z"));

        ReminderEntity saved = new ReminderEntity();
        saved.setTask(task);
        saved.setUser(user);
        saved.setType(ReminderType.DUE_SOON);
        saved.setTriggerMode(ReminderTriggerMode.RELATIVE_DUE);
        saved.setMinutesBeforeDue(30);
        saved.setTriggerAt(Instant.parse("2026-04-20T09:30:00Z"));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(reminderRepository.save(any(ReminderEntity.class))).thenReturn(saved);

        ReminderCreateRequest request = new ReminderCreateRequest(
                ReminderType.DUE_SOON,
                ReminderTriggerMode.RELATIVE_DUE,
                30,
                null,
                ReminderChannel.IN_APP
        );

        ReminderResponse response = reminderService.createForTask(taskId, request);

        assertThat(response.triggerAt()).isEqualTo(Instant.parse("2026-04-20T09:30:00Z"));
    }

    @Test
    void createForTask_shouldFailWithoutDueDateForRelative() {
        UUID taskId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        TaskEntity task = new TaskEntity();
        task.setCreatedBy(user);
        task.setDueAt(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        ReminderCreateRequest request = new ReminderCreateRequest(
                ReminderType.CUSTOM,
                ReminderTriggerMode.RELATIVE_DUE,
                60,
                null,
                ReminderChannel.IN_APP
        );

        assertThatThrownBy(() -> reminderService.createForTask(taskId, request))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void createForAssignment_shouldUseAssignmentDateAndTime() {
        UUID assignmentId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setTimezone("UTC");

        TaskEntity task = new TaskEntity();
        task.setCreatedBy(user);

        DailyTaskAssignmentEntity assignment = new DailyTaskAssignmentEntity();
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setAssignmentDate(LocalDate.of(2026, 4, 21));
        assignment.setPlannedStartTime(LocalTime.of(11, 0));

        ReminderEntity saved = new ReminderEntity();
        saved.setTask(task);
        saved.setDailyAssignment(assignment);
        saved.setUser(user);
        saved.setType(ReminderType.CUSTOM);
        saved.setTriggerMode(ReminderTriggerMode.RELATIVE_DUE);
        saved.setMinutesBeforeDue(15);
        saved.setTriggerAt(Instant.parse("2026-04-21T10:45:00Z"));

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(reminderRepository.save(any(ReminderEntity.class))).thenReturn(saved);

        ReminderCreateRequest request = new ReminderCreateRequest(
                ReminderType.CUSTOM,
                ReminderTriggerMode.RELATIVE_DUE,
                15,
                null,
                ReminderChannel.IN_APP
        );

        ReminderResponse response = reminderService.createForAssignment(assignmentId, request);

        assertThat(response.triggerAt()).isEqualTo(Instant.parse("2026-04-21T10:45:00Z"));
    }

    @Test
    void dispatchDueReminders_shouldMarkAsSent() {
        ReminderEntity reminder = new ReminderEntity();
        reminder.setStatus(ReminderStatus.SCHEDULED);
        reminder.setAttemptCount(0);

        when(reminderRepository.findByStatusAndTriggerAtLessThanEqual(eq(ReminderStatus.SCHEDULED), any())).thenReturn(List.of(reminder));

        int count = reminderService.dispatchDueReminders();

        assertThat(count).isEqualTo(1);
        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.SENT);
        assertThat(reminder.getAttemptCount()).isEqualTo(1);
        verify(reminderRepository).save(reminder);
    }
}
