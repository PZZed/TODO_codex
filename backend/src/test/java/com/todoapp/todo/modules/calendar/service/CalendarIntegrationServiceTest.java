package com.todoapp.todo.modules.calendar.service;

import com.todoapp.todo.modules.calendar.domain.CalendarIntegrationEntity;
import com.todoapp.todo.modules.calendar.dto.CalendarIntegrationResponse;
import com.todoapp.todo.modules.calendar.repository.CalendarIntegrationRepository;
import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarIntegrationServiceTest {

    @Mock
    private CalendarIntegrationRepository calendarIntegrationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private DailyTaskAssignmentRepository assignmentRepository;

    @InjectMocks
    private CalendarIntegrationService calendarIntegrationService;

    @Test
    void enableIcs_shouldCreateIntegrationIfMissing() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();

        CalendarIntegrationEntity saved = new CalendarIntegrationEntity();
        saved.setUser(user);
        saved.setExportToken("token1234567890");
        saved.setEnabled(true);

        when(calendarIntegrationRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(calendarIntegrationRepository.save(any(CalendarIntegrationEntity.class))).thenReturn(saved);

        CalendarIntegrationResponse response = calendarIntegrationService.enableIcs(userId, "http://localhost:8080");

        assertThat(response.enabled()).isTrue();
        assertThat(response.publicIcsUrl()).contains("/api/v1/calendar/ics/");
    }

    @Test
    void exportIcsByToken_shouldReturnCalendarTextWithTaskEvent() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        TaskEntity task = new TaskEntity();
        task.setTitle("Demo");
        task.setDueAt(Instant.parse("2026-04-20T10:00:00Z"));

        CalendarIntegrationEntity integration = new CalendarIntegrationEntity();
        integration.setUser(user);
        integration.setEnabled(true);
        integration.setExportToken("token");

        when(calendarIntegrationRepository.findByExportTokenAndEnabledTrue("token")).thenReturn(Optional.of(integration));
        when(taskRepository.findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(any(), any(), any())).thenReturn(List.of(task));
        when(assignmentRepository.findByUserIdAndAssignmentDateBetween(any(), any(), any())).thenReturn(List.of());

        String ics = calendarIntegrationService.exportIcsByToken("token", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("SUMMARY:Demo");
    }

    @Test
    void exportIcsByToken_shouldIncludeAssignmentEvent() {
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        TaskEntity task = new TaskEntity();
        task.setTitle("Call");

        DailyTaskAssignmentEntity assignment = new DailyTaskAssignmentEntity();
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setAssignmentDate(LocalDate.of(2026, 4, 22));

        CalendarIntegrationEntity integration = new CalendarIntegrationEntity();
        integration.setUser(user);
        integration.setEnabled(true);
        integration.setExportToken("token");

        when(calendarIntegrationRepository.findByExportTokenAndEnabledTrue("token")).thenReturn(Optional.of(integration));
        when(taskRepository.findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(any(), any(), any())).thenReturn(List.of());
        when(assignmentRepository.findByUserIdAndAssignmentDateBetween(any(), any(), any())).thenReturn(List.of(assignment));

        String ics = calendarIntegrationService.exportIcsByToken("token", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertThat(ics).contains("SUMMARY:Call");
    }
}
