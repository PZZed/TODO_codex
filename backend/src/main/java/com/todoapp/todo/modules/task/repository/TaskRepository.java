package com.todoapp.todo.modules.task.repository;

import com.todoapp.todo.modules.task.domain.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByTaskListId(UUID taskListId);
}
