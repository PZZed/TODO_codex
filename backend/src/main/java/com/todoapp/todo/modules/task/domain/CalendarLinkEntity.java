package com.todoapp.todo.modules.task.domain;

import com.todoapp.todo.modules.user.domain.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendar_links")
public class CalendarLinkEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_assignment_id")
    private DailyTaskAssignmentEntity dailyAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CalendarProvider provider;

    @Column(nullable = false)
    private String calendarId;

    @Column(nullable = false)
    private String externalEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SyncDirection syncDirection = SyncDirection.TODO_TO_CALENDAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CalendarSyncStatus syncStatus = CalendarSyncStatus.PENDING;

    private Instant lastSyncedAt;

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
