package com.todoapp.todo.modules.task.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recurring_task_rules")
public class RecurringTaskRuleEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private TaskEntity task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurrenceFrequency frequency;

    @Column(nullable = false)
    private Integer intervalValue = 1;

    private String daysOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

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
    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }
    public Integer getIntervalValue() { return intervalValue; }
    public void setIntervalValue(Integer intervalValue) { this.intervalValue = intervalValue; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public Integer getMonthOfYear() { return monthOfYear; }
    public void setMonthOfYear(Integer monthOfYear) { this.monthOfYear = monthOfYear; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
