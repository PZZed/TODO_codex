package com.todoapp.todo.modules.task.dto;

import java.time.LocalDate;
import java.util.List;

public record TaskDayResponse(
        LocalDate date,
        List<TaskResponse> tasks
) {
}
