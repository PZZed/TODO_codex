package com.todoapp.todo.modules.calendar.dto;

import java.util.UUID;

public record CalendarIntegrationResponse(
        UUID userId,
        boolean enabled,
        String exportToken,
        String publicIcsUrl
) {
}
