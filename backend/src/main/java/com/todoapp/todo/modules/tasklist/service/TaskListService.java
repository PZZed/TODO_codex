package com.todoapp.todo.modules.tasklist.service;

import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.dto.TaskListCreateRequest;
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

        TaskListEntity entity = new TaskListEntity();
        entity.setOwner(owner);
        entity.setName(request.name());
        entity.setColor(request.color());

        return taskListMapper.toResponse(taskListRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TaskListResponse> findByOwner(UUID ownerUserId) {
        return taskListRepository.findByOwnerId(ownerUserId).stream().map(taskListMapper::toResponse).toList();
    }
}
