package com.todoapp.todo.modules.user.mapper;

import com.todoapp.todo.modules.user.domain.UserEntity;
import com.todoapp.todo.modules.user.dto.UserCreateRequest;
import com.todoapp.todo.modules.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", expression = "java(hashPassword(request.password()))")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    UserResponse toResponse(UserEntity entity);

    default String hashPassword(String raw) {
        return "{noop}" + raw;
    }
}
