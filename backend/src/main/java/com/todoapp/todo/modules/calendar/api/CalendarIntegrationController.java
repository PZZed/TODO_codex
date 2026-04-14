package com.todoapp.todo.modules.calendar.api;

import com.todoapp.todo.modules.calendar.dto.CalendarIntegrationResponse;
import com.todoapp.todo.modules.calendar.service.CalendarIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarIntegrationController {

    private final CalendarIntegrationService calendarIntegrationService;

    public CalendarIntegrationController(CalendarIntegrationService calendarIntegrationService) {
        this.calendarIntegrationService = calendarIntegrationService;
    }

    @Operation(summary = "Enable ICS integration for a user")
    @PostMapping("/integrations/ics")
    public CalendarIntegrationResponse enableIcs(@RequestParam UUID userId,
                                                 @RequestHeader(value = "X-Base-Url", defaultValue = "http://localhost:8080") String baseUrl) {
        return calendarIntegrationService.enableIcs(userId, baseUrl);
    }

    @Operation(summary = "Rotate ICS export token")
    @PostMapping("/integrations/ics/{userId}/rotate-token")
    public CalendarIntegrationResponse rotateToken(@PathVariable UUID userId,
                                                   @RequestHeader(value = "X-Base-Url", defaultValue = "http://localhost:8080") String baseUrl) {
        return calendarIntegrationService.rotateToken(userId, baseUrl);
    }

    @Operation(summary = "Public ICS export endpoint")
    @GetMapping("/ics/{token}")
    public ResponseEntity<String> exportIcs(
            @PathVariable String token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        String ics = calendarIntegrationService.exportIcsByToken(token, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=todo-calendar.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics);
    }
}
