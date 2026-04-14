package com.todoapp.todo.modules.calendar.service;

import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.calendar.domain.CalendarIntegrationEntity;
import com.todoapp.todo.modules.calendar.dto.CalendarIntegrationResponse;
import com.todoapp.todo.modules.calendar.repository.CalendarIntegrationRepository;
import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CalendarIntegrationService {

    private final CalendarIntegrationRepository calendarIntegrationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DailyTaskAssignmentRepository assignmentRepository;

    public CalendarIntegrationService(CalendarIntegrationRepository calendarIntegrationRepository,
                                      UserRepository userRepository,
                                      TaskRepository taskRepository,
                                      DailyTaskAssignmentRepository assignmentRepository) {
        this.calendarIntegrationRepository = calendarIntegrationRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public CalendarIntegrationResponse enableIcs(UUID userId, String baseUrl) {
        CalendarIntegrationEntity integration = calendarIntegrationRepository.findByUserId(userId)
                .orElseGet(() -> createIntegration(userId));
        integration.setEnabled(true);
        CalendarIntegrationEntity saved = calendarIntegrationRepository.save(integration);
        return toResponse(saved, baseUrl);
    }

    @Transactional
    public CalendarIntegrationResponse rotateToken(UUID userId, String baseUrl) {
        CalendarIntegrationEntity integration = calendarIntegrationRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar integration not found for user: " + userId));
        integration.setExportToken(newToken());
        CalendarIntegrationEntity saved = calendarIntegrationRepository.save(integration);
        return toResponse(saved, baseUrl);
    }

    @Transactional(readOnly = true)
    public String exportIcsByToken(String token, LocalDate from, LocalDate to) {
        CalendarIntegrationEntity integration = calendarIntegrationRepository.findByExportTokenAndEnabledTrue(token)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar integration token not found"));
        UUID userId = integration.getUser().getId();

        LocalDate safeFrom = from == null ? LocalDate.now().minusMonths(1) : from;
        LocalDate safeTo = to == null ? LocalDate.now().plusMonths(6) : to;
        if (safeTo.isBefore(safeFrom)) {
            throw new IllegalArgumentException("to must be >= from");
        }

        return buildIcs(userId, safeFrom, safeTo);
    }

    private CalendarIntegrationEntity createIntegration(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        CalendarIntegrationEntity integration = new CalendarIntegrationEntity();
        integration.setUser(user);
        integration.setExportToken(newToken());
        integration.setEnabled(true);
        return integration;
    }

    private String buildIcs(UUID userId, LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//TODO App//ICS Export V1//FR\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");

        ZoneId zoneId = ZoneId.of("UTC");
        Instant start = from.atStartOfDay(zoneId).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(zoneId).minusSeconds(1).toInstant();

        List<TaskEntity> tasks = taskRepository.findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(userId, start, end);
        for (TaskEntity task : tasks) {
            appendTaskEvent(sb, task);
        }

        List<DailyTaskAssignmentEntity> assignments = assignmentRepository.findByUserIdAndAssignmentDateBetween(userId, from, to);
        for (DailyTaskAssignmentEntity assignment : assignments) {
            appendAssignmentEvent(sb, assignment);
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private void appendTaskEvent(StringBuilder sb, TaskEntity task) {
        Instant start = task.getStartAt() != null ? task.getStartAt() : task.getDueAt();
        Instant end = task.getDueAt() != null ? task.getDueAt() : start.plus(Duration.ofMinutes(30));

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append("task-").append(task.getId()).append("@todo-app\r\n");
        sb.append("DTSTAMP:").append(toIcsDateTime(Instant.now())).append("\r\n");
        sb.append("DTSTART:").append(toIcsDateTime(start)).append("\r\n");
        sb.append("DTEND:").append(toIcsDateTime(end)).append("\r\n");
        sb.append("SUMMARY:").append(escape(task.getTitle())).append("\r\n");
        if (task.getDescription() != null) {
            sb.append("DESCRIPTION:").append(escape(task.getDescription())).append("\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private void appendAssignmentEvent(StringBuilder sb, DailyTaskAssignmentEntity assignment) {
        LocalDate date = assignment.getAssignmentDate();
        LocalTime startTime = assignment.getPlannedStartTime() != null ? assignment.getPlannedStartTime() : LocalTime.of(9, 0);
        LocalTime endTime = assignment.getPlannedEndTime() != null ? assignment.getPlannedEndTime() : startTime.plusMinutes(30);

        ZonedDateTime start = ZonedDateTime.of(date, startTime, ZoneId.of("UTC"));
        ZonedDateTime end = ZonedDateTime.of(date, endTime, ZoneId.of("UTC"));

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append("assignment-").append(assignment.getId()).append("@todo-app\r\n");
        sb.append("DTSTAMP:").append(toIcsDateTime(Instant.now())).append("\r\n");
        sb.append("DTSTART:").append(toIcsDateTime(start.toInstant())).append("\r\n");
        sb.append("DTEND:").append(toIcsDateTime(end.toInstant())).append("\r\n");
        sb.append("SUMMARY:").append(escape(assignment.getTask().getTitle())).append("\r\n");
        if (assignment.getNote() != null) {
            sb.append("DESCRIPTION:").append(escape(assignment.getNote())).append("\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private String toIcsDateTime(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private CalendarIntegrationResponse toResponse(CalendarIntegrationEntity entity, String baseUrl) {
        String url = baseUrl + "/api/v1/calendar/ics/" + entity.getExportToken();
        return new CalendarIntegrationResponse(entity.getUser().getId(), entity.isEnabled(), entity.getExportToken(), url);
    }
}
