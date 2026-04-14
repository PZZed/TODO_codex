package com.todoapp.todo.modules.tasklist.service;

import com.todoapp.todo.common.exception.BusinessConflictException;
import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.dto.TaskListCreateRequest;
import com.todoapp.todo.modules.tasklist.dto.TaskListRenameRequest;
import com.todoapp.todo.modules.tasklist.dto.TaskListResponse;
import com.todoapp.todo.modules.tasklist.mapper.TaskListMapper;
import com.todoapp.todo.modules.tasklist.repository.TaskListRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskListMapper taskListMapper;

    @InjectMocks
    private TaskListService taskListService;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Test
    void create_shouldCreateTaskList_whenValidRequest() {
        TaskListCreateRequest request = new TaskListCreateRequest(ownerId, "Travail", "#111", 1);
        UserEntity owner = new UserEntity();
        owner.setEmail("john@doe.com");
        owner.setDisplayName("John");
        owner.setPasswordHash("hash");
        owner.setTimezone("Europe/Paris");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(taskListRepository.existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(ownerId, "Travail")).thenReturn(false);

        TaskListEntity saved = new TaskListEntity();
        saved.setOwner(owner);
        saved.setName("Travail");
        when(taskListRepository.save(any(TaskListEntity.class))).thenReturn(saved);
        when(taskListMapper.toResponse(saved)).thenReturn(new TaskListResponse(UUID.randomUUID(), ownerId, "Travail", "#111", false, 1, null, null));

        TaskListResponse response = taskListService.create(request);

        assertThat(response.name()).isEqualTo("Travail");
        ArgumentCaptor<TaskListEntity> captor = ArgumentCaptor.forClass(TaskListEntity.class);
        verify(taskListRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Travail");
    }

    @Test
    void create_shouldThrowConflict_whenDuplicateName() {
        TaskListCreateRequest request = new TaskListCreateRequest(ownerId, "Travail", null, 0);
        UserEntity owner = new UserEntity();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(taskListRepository.existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(ownerId, "Travail")).thenReturn(true);

        assertThatThrownBy(() -> taskListService.create(request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void rename_shouldThrowNotFound_whenUnknownList() {
        UUID taskListId = UUID.randomUUID();
        when(taskListRepository.findById(taskListId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.rename(taskListId, new TaskListRenameRequest("New")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldArchiveTaskList_whenActive() {
        UUID taskListId = UUID.randomUUID();
        TaskListEntity entity = new TaskListEntity();
        entity.setArchived(false);

        when(taskListRepository.findById(taskListId)).thenReturn(Optional.of(entity));

        taskListService.delete(taskListId);

        assertThat(entity.isArchived()).isTrue();
        verify(taskListRepository).save(entity);
    }

    @Test
    void findByOwner_shouldReturnMappedLists() {
        TaskListEntity list = new TaskListEntity();
        when(taskListRepository.findByOwnerIdAndArchivedFalseOrderByPositionAsc(ownerId)).thenReturn(List.of(list));
        when(taskListMapper.toResponse(list)).thenReturn(new TaskListResponse(UUID.randomUUID(), ownerId, "A", null, false, 0, null, null));

        List<TaskListResponse> result = taskListService.findByOwner(ownerId);

        assertThat(result).hasSize(1);
        verify(taskListRepository).findByOwnerIdAndArchivedFalseOrderByPositionAsc(ownerId);
    }
}
