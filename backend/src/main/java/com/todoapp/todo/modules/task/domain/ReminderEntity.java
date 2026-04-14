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

    @Column(nullable = false)
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

    public UUID getId() { return id; }
    public TaskEntity getTask() { return task; }
    public void setTask(TaskEntity task) { this.task = task; }
    public DailyTaskAssignmentEntity getDailyAssignment() { return dailyAssignment; }
    public void setDailyAssignment(DailyTaskAssignmentEntity dailyAssignment) { this.dailyAssignment = dailyAssignment; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }
    public ReminderTriggerMode getTriggerMode() { return triggerMode; }
    public void setTriggerMode(ReminderTriggerMode triggerMode) { this.triggerMode = triggerMode; }
    public Integer getMinutesBeforeDue() { return minutesBeforeDue; }
    public void setMinutesBeforeDue(Integer minutesBeforeDue) { this.minutesBeforeDue = minutesBeforeDue; }
    public Instant getTriggerAt() { return triggerAt; }
    public void setTriggerAt(Instant triggerAt) { this.triggerAt = triggerAt; }
    public ReminderChannel getChannel() { return channel; }
    public void setChannel(ReminderChannel channel) { this.channel = channel; }
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public Instant getCreatedAt() { return createdAt; }
}
