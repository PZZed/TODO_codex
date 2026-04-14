package com.todoapp.todo.modules.task.repository;

import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyTaskAssignmentRepository extends JpaRepository<DailyTaskAssignmentEntity, UUID> {
    Optional<DailyTaskAssignmentEntity> findByTaskIdAndUserIdAndAssignmentDate(UUID taskId, UUID userId, LocalDate assignmentDate);
    List<DailyTaskAssignmentEntity> findByUserIdAndAssignmentDate(UUID userId, LocalDate assignmentDate);
    List<DailyTaskAssignmentEntity> findByUserIdAndAssignmentDateBetween(UUID userId, LocalDate from, LocalDate to);
}
