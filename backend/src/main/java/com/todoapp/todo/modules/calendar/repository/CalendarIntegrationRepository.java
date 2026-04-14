package com.todoapp.todo.modules.calendar.repository;

import com.todoapp.todo.modules.calendar.domain.CalendarIntegrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CalendarIntegrationRepository extends JpaRepository<CalendarIntegrationEntity, UUID> {
    Optional<CalendarIntegrationEntity> findByUserId(UUID userId);
    Optional<CalendarIntegrationEntity> findByExportTokenAndEnabledTrue(String exportToken);
}
