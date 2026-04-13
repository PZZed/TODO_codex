package com.todoapp.todo.modules.task.service;

import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.domain.TaskPriority;
import com.todoapp.todo.modules.task.domain.TaskStatus;
import com.todoapp.todo.modules.task.dto.TaskCreateRequest;
import com.todoapp.todo.modules.task.dto.TaskResponse;
import com.todoapp.todo.modules.task.mapper.TaskMapper;
import com.todoapp.todo.modules.task.repository.TaskRepository;
import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.repository.TaskListRepository;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       TaskListRepository taskListRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        TaskListEntity taskList = taskListRepository.findById(request.taskListId())
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found: " + request.taskListId()));
        UserEntity creator = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdByUserId()));

        TaskEntity entity = new TaskEntity();
        entity.setTaskList(taskList);
        entity.setCreatedBy(creator);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        entity.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        entity.setStartAt(request.startAt());
        entity.setDueAt(request.dueAt());
        entity.setAllDay(request.allDay());

        return taskMapper.toResponse(taskRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByTaskList(UUID taskListId) {
        return taskRepository.findByTaskListId(taskListId).stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        return taskMapper.toResponse(task);
    }
}
