package com.josewolf.task_api.dto.responsedto;

import com.josewolf.task_api.model.TaskStatus;

public record TaskResponseDTO(

    Long id,
    String title,
    String description,
    TaskStatus taskStatus,
    Long userId

) { }
