package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.task.domain.AssignmentOrigin;
import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.domain.TaskPriority;
import com.todoapp.todo.modules.task.domain.TaskStatus;
import com.todoapp.todo.modules.task.dto.*;
import com.todoapp.todo.modules.task.mapper.TaskMapper;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.repository.TaskListRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final DailyTaskAssignmentRepository assignmentRepository;
    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       DailyTaskAssignmentRepository assignmentRepository,
                       TaskListRepository taskListRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        TaskListEntity taskList = taskListRepository.findById(request.taskListId())
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found: " + request.taskListId()));
        UserEntity creator = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdByUserId()));

        validateTaskDates(request.startAt(), request.dueAt());

        TaskEntity entity = new TaskEntity();
        entity.setTaskList(taskList);
        entity.setCreatedBy(creator);
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description());
        entity.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        entity.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        entity.setStartAt(request.startAt());
        entity.setDueAt(request.dueAt());
        entity.setAllDay(request.allDay());

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public TaskResponse update(UUID taskId, TaskUpdateRequest request) {
        TaskEntity entity = getActiveTask(taskId);

        Instant startAt = request.startAt() != null ? request.startAt() : entity.getStartAt();
        Instant dueAt = request.dueAt() != null ? request.dueAt() : entity.getDueAt();
        validateTaskDates(startAt, dueAt);

        if (request.title() != null) entity.setTitle(request.title().trim());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.priority() != null) entity.setPriority(request.priority());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.startAt() != null) entity.setStartAt(request.startAt());
        if (request.dueAt() != null) entity.setDueAt(request.dueAt());
        if (request.allDay() != null) entity.setAllDay(request.allDay());

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public TaskResponse markCompleted(UUID taskId) {
        TaskEntity entity = getActiveTask(taskId);
        if (entity.getStatus() == TaskStatus.DONE) {
            throw new BusinessConflictException("Task is already completed");
        }
        entity.setStatus(TaskStatus.DONE);
        entity.setCompletedAt(Instant.now());
        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional
    public void delete(UUID taskId) {
        TaskEntity entity = getActiveTask(taskId);
        entity.setDeletedAt(Instant.now());
        taskRepository.save(entity);
    }

    @Transactional
    public TaskAssignmentResponse assignToDate(UUID taskId, TaskAssignmentRequest request) {
        TaskEntity task = getActiveTask(taskId);

        if (request.plannedStartTime() != null && request.plannedEndTime() != null
                && request.plannedEndTime().isBefore(request.plannedStartTime())) {
            throw new BusinessConflictException("plannedEndTime must be greater than or equal to plannedStartTime");
        }

        UUID userId = task.getCreatedBy().getId();
        assignmentRepository.findByTaskIdAndUserIdAndAssignmentDate(taskId, userId, request.assignmentDate())
                .ifPresent(existing -> {
                    throw new BusinessConflictException("Task already assigned for this date");
                });

        DailyTaskAssignmentEntity entity = new DailyTaskAssignmentEntity();
        entity.setTask(task);
        entity.setUser(task.getCreatedBy());
        entity.setAssignmentDate(request.assignmentDate());
        entity.setPlannedStartTime(request.plannedStartTime());
        entity.setPlannedEndTime(request.plannedEndTime());
        entity.setOrigin(AssignmentOrigin.MANUAL);
        entity.setNote(request.note());

        return taskMapper.toAssignmentResponse(assignmentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByTaskList(UUID taskListId) {
        return taskRepository.findByTaskListIdAndDeletedAtIsNull(taskListId)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        return taskMapper.toResponse(getActiveTask(id));
    }

    @Transactional(readOnly = true)
    public TaskDayResponse findTasksForDay(UUID userId, LocalDate date, ZoneId zoneId) {
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).minusNanos(1).toInstant();

        List<TaskResponse> tasks = taskRepository.findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(userId, start, end)
                .stream()
                .map(taskMapper::toResponse)
                .toList();

        return new TaskDayResponse(date, tasks);
    }

    private TaskEntity getActiveTask(UUID taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (task.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private void validateTaskDates(Instant startAt, Instant dueAt) {
        if (startAt != null && dueAt != null && dueAt.isBefore(startAt)) {
            throw new BusinessConflictException("dueAt must be greater than or equal to startAt");
        }
    }
}
