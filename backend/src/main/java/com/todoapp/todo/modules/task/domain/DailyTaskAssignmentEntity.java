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

    public UUID getId() { return id; }
    public TaskEntity getTask() { return task; }
    public void setTask(TaskEntity task) { this.task = task; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public LocalDate getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }
    public LocalTime getPlannedStartTime() { return plannedStartTime; }
    public void setPlannedStartTime(LocalTime plannedStartTime) { this.plannedStartTime = plannedStartTime; }
    public LocalTime getPlannedEndTime() { return plannedEndTime; }
    public void setPlannedEndTime(LocalTime plannedEndTime) { this.plannedEndTime = plannedEndTime; }
    public AssignmentOrigin getOrigin() { return origin; }
    public void setOrigin(AssignmentOrigin origin) { this.origin = origin; }
    public DailyAssignmentStatus getStatusOnDay() { return statusOnDay; }
    public void setStatusOnDay(DailyAssignmentStatus statusOnDay) { this.statusOnDay = statusOnDay; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
