package com.josewolf.task_api.dto.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
        @Schema(description = "Id único gerado pelo banco de dados.", example = "1")
        Long id,
        @Schema(description = "Retorna o username escrito pelo usuário.", example = "Teste")
        String username,
        @Schema(description = "Retorna o email escrito pelo usuário.", example = "teste@gmail.com")
        String email
) {
}
