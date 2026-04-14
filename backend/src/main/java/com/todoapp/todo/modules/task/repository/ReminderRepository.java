package com.todoapp.todo.modules.task.repository;

import com.todoapp.todo.modules.task.domain.ReminderEntity;
import com.todoapp.todo.modules.task.domain.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<ReminderEntity, UUID> {
    List<ReminderEntity> findByStatusAndTriggerAtLessThanEqual(ReminderStatus status, Instant triggerAt);
    List<ReminderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
