package com.todoapp.todo.modules.user.service;

import com.todoapp.todo.common.exception.ResourceNotFoundException;
import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.dto.UserCreateRequest;
import com.todoapp.todo.modules.user.dto.UserResponse;
import com.todoapp.todo.modules.user.mapper.UserMapper;
import com.todoapp.todo.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return userMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }
}
