package com.josewolf.task_api.dto.requestdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(
        @NotBlank
        String username,

        @Email(message = "E-mail inválido")
        @NotBlank
        String email
) {
}
