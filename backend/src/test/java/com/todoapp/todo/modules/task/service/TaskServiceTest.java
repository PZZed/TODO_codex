package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.domain.TaskStatus;
import com.todoapp.todo.modules.task.dto.*;
import com.todoapp.todo.modules.task.mapper.TaskMapper;
import com.todoapp.todo.modules.task.repository.DailyTaskAssignmentRepository;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.repository.TaskListRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private DailyTaskAssignmentRepository assignmentRepository;
    @Mock private TaskListRepository taskListRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks private TaskService taskService;

    private UUID taskId;
    private UUID listId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        listId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void create_shouldFail_whenDueBeforeStart() {
        TaskCreateRequest request = new TaskCreateRequest(
                listId,
                userId,
                "task",
                null,
                null,
                null,
                Instant.parse("2026-04-15T10:00:00Z"),
                Instant.parse("2026-04-14T10:00:00Z"),
                false
        );

        TaskListEntity list = new TaskListEntity();
        UserEntity user = new UserEntity();

        when(taskListRepository.findById(listId)).thenReturn(Optional.of(list));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void update_shouldUpdateFields() {
        TaskEntity entity = new TaskEntity();
        entity.setTitle("old");
        entity.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(entity));
        when(taskRepository.save(entity)).thenReturn(entity);
        when(taskMapper.toResponse(entity)).thenReturn(new TaskResponse(taskId, listId, userId, "new", null, TaskStatus.IN_PROGRESS, null, null, null, null, null, false, null, null));

        TaskUpdateRequest request = new TaskUpdateRequest("new", null, TaskStatus.IN_PROGRESS, null, null, null, null);
        TaskResponse response = taskService.update(taskId, request);

        assertThat(response.title()).isEqualTo("new");
        assertThat(entity.getTitle()).isEqualTo("new");
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void delete_shouldSoftDelete() {
        TaskEntity entity = new TaskEntity();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(entity));

        taskService.delete(taskId);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(taskRepository).save(entity);
    }

    @Test
    void markCompleted_shouldSetDoneAndCompletedAt() {
        TaskEntity entity = new TaskEntity();
        entity.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(entity));
        when(taskRepository.save(entity)).thenReturn(entity);
        when(taskMapper.toResponse(entity)).thenReturn(new TaskResponse(taskId, listId, userId, "x", null, TaskStatus.DONE, null, null, null, Instant.now(), null, false, null, null));

        TaskResponse response = taskService.markCompleted(taskId);

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(entity.getCompletedAt()).isNotNull();
    }

    @Test
    void assignToDate_shouldFailOnDuplicate() {
        TaskEntity task = new TaskEntity();
        UserEntity creator = new UserEntity();
        task.setCreatedBy(creator);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.findByTaskIdAndUserIdAndAssignmentDate(any(), any(), any()))
                .thenReturn(Optional.of(new DailyTaskAssignmentEntity()));

        TaskAssignmentRequest request = new TaskAssignmentRequest(LocalDate.now(), null, null, null);

        assertThatThrownBy(() -> taskService.assignToDate(taskId, request))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void findTasksForDay_shouldReturnResults() {
        TaskEntity task = new TaskEntity();
        when(taskRepository.findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(eq(userId), any(), any())).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(new TaskResponse(taskId, listId, userId, "x", null, TaskStatus.TODO, null, null, null, null, null, false, null, null));

        TaskDayResponse response = taskService.findTasksForDay(userId, LocalDate.of(2026, 4, 15), ZoneId.of("UTC"));

        assertThat(response.tasks()).hasSize(1);
    }

    @Test
    void findById_shouldThrowNotFoundWhenDeleted() {
        TaskEntity task = new TaskEntity();
        task.setDeletedAt(Instant.now());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
