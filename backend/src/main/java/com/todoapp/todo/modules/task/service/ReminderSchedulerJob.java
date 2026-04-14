package com.todoapp.todo.modules.task.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderSchedulerJob {

    private final ReminderService reminderService;

    public ReminderSchedulerJob(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${reminder.scheduler.fixed-delay-ms:60000}")
    public void dispatchDueReminders() {
        reminderService.dispatchDueReminders();
    }
}
