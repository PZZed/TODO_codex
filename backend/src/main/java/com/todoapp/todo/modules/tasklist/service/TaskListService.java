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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final TaskListMapper taskListMapper;

    public TaskListService(TaskListRepository taskListRepository, UserRepository userRepository, TaskListMapper taskListMapper) {
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
        this.taskListMapper = taskListMapper;
    }

    @Transactional
    public TaskListResponse create(TaskListCreateRequest request) {
        UserEntity owner = userRepository.findById(request.ownerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + request.ownerUserId()));

        if (taskListRepository.existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(request.ownerUserId(), request.name())) {
            throw new BusinessConflictException("Task list name already exists for owner");
        }

        TaskListEntity entity = new TaskListEntity();
        entity.setOwner(owner);
        entity.setName(request.name().trim());
        entity.setColor(request.color());
        entity.setPosition(request.position() == null ? 0 : request.position());

        return taskListMapper.toResponse(taskListRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TaskListResponse> findByOwner(UUID ownerUserId) {
        return taskListRepository.findByOwnerIdAndArchivedFalseOrderByPositionAsc(ownerUserId)
                .stream()
                .map(taskListMapper::toResponse)
                .toList();
    }

    @Transactional
    public TaskListResponse rename(UUID taskListId, TaskListRenameRequest request) {
        TaskListEntity entity = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found: " + taskListId));

        if (entity.isArchived()) {
            throw new BusinessConflictException("Cannot rename an archived task list");
        }

        UUID ownerId = entity.getOwner().getId();
        String newName = request.name().trim();
        boolean duplicate = taskListRepository.existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(ownerId, newName);
        if (duplicate && !entity.getName().equalsIgnoreCase(newName)) {
            throw new BusinessConflictException("Task list name already exists for owner");
        }

        entity.setName(newName);
        return taskListMapper.toResponse(taskListRepository.save(entity));
    }

    @Transactional
    public void delete(UUID taskListId) {
        TaskListEntity entity = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found: " + taskListId));

        if (entity.isArchived()) {
            throw new BusinessConflictException("Task list is already archived");
        }

        entity.setArchived(true);
        taskListRepository.save(entity);
    }
}
