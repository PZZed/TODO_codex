package com.todoapp.todo.modules.task.repository;

import com.todoapp.todo.modules.task.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByTaskListIdAndDeletedAtIsNull(UUID taskListId);
    List<TaskEntity> findByCreatedByIdAndDeletedAtIsNullAndDueAtBetween(UUID userId, Instant start, Instant end);
}
