package com.todoapp.todo.modules.task.mapper;

import com.todoapp.todo.modules.task.domain.DailyTaskAssignmentEntity;
import com.todoapp.todo.modules.task.domain.TaskEntity;
import com.todoapp.todo.modules.task.dto.TaskAssignmentResponse;
import com.todoapp.todo.modules.task.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "taskListId", source = "taskList.id")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    TaskResponse toResponse(TaskEntity entity);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "userId", source = "user.id")
    TaskAssignmentResponse toAssignmentResponse(DailyTaskAssignmentEntity entity);
}
