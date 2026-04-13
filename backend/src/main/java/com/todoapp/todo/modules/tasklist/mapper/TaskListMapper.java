package com.todoapp.todo.modules.tasklist.mapper;

import com.todoapp.todo.modules.tasklist.domain.TaskListEntity;
import com.todoapp.todo.modules.tasklist.dto.TaskListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskListMapper {

    @Mapping(target = "ownerUserId", source = "owner.id")
    TaskListResponse toResponse(TaskListEntity entity);
}
