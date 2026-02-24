package com.josewolf.task_api.dto;

import com.josewolf.task_api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record TaskStatusRequestDTO(
        @Schema(description = "Status atual da tarefa", allowableValues = {"PENDING", "COMPLETED"})
        TaskStatus taskStatus
) {
}
