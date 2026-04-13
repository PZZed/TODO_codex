package com.todoapp.todo.modules.task.domain;

import com.todoapp.todo.modules.user.domain.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "daily_task_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_task_user_day", columnNames = {"task_id", "user_id", "assignment_date"}))
public class DailyTaskAssignmentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssignmentOrigin origin = AssignmentOrigin.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DailyAssignmentStatus statusOnDay = DailyAssignmentStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
