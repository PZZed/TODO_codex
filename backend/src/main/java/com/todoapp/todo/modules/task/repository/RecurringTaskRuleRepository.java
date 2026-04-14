package com.todoapp.todo.modules.task.repository;

import com.todoapp.todo.modules.task.domain.RecurringTaskRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTaskRuleRepository extends JpaRepository<RecurringTaskRuleEntity, UUID> {
    Optional<RecurringTaskRuleEntity> findByTaskId(UUID taskId);
    List<RecurringTaskRuleEntity> findByTaskCreatedByIdAndActiveTrue(UUID userId);
}
