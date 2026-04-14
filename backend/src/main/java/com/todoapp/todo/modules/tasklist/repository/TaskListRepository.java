package com.todoapp.todo.modules.tasklist.repository;

import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskListRepository extends JpaRepository<TaskListEntity, UUID> {
    List<TaskListEntity> findByOwnerIdAndArchivedFalseOrderByPositionAsc(UUID ownerId);
    boolean existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(UUID ownerId, String name);
}
