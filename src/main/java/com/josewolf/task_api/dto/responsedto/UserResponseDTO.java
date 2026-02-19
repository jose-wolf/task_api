package com.josewolf.task_api.dto.responsedto;

public record UserResponseDTO(
        Long id,
        String username,
        String email
) {
}
