package com.josewolf.task_api.dto;

import com.josewolf.task_api.model.TaskStatus;

public record TaskStatusRequestDTO(
        TaskStatus taskStatus
) {
}
