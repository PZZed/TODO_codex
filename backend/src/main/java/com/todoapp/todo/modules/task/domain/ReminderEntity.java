package com.todoapp.todo.modules.task.domain;

import com.todoapp.todo.modules.user.domain.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminders")
public class ReminderEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_assignment_id")
    private DailyTaskAssignmentEntity dailyAssignment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderTriggerMode triggerMode;

    private Integer minutesBeforeDue;
    private Instant triggerAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderChannel channel = ReminderChannel.IN_APP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReminderStatus status = ReminderStatus.SCHEDULED;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    private Instant lastAttemptAt;

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
