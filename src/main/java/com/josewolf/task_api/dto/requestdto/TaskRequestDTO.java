package com.josewolf.task_api.dto.requestdto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequestDTO(
    @NotBlank(message = "A tarefa deve ter um título")
    @Schema(description = "Nome do título para a task", example = "Estudar Spring Security")
    String title,

    @NotBlank(message = "A descrição deve conter algum conteúdo")
    @Schema(description = "Texto da descrição para a task", example = "Compreender qual o objetivo da segurança, e como implementar.")
    String description,

    @NotNull(message = "O ID do usuário é obrigatório")
    @Schema(description = "Relacionamento da task com o usuário", example = "A task de id 1 se relaciona com usuário de id 7.")
    Long userId
) {
}
