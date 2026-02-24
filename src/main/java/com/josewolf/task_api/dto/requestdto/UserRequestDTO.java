package com.josewolf.task_api.dto.requestdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(
        @NotBlank
        @Schema(description = "Nome do usuário", example = "José Wolf")
        String username,

        @Email(message = "E-mail inválido")
        @NotBlank
        @Schema(description = "Nome do email", example = "teste@gmail.com")
        String email
) {
}
