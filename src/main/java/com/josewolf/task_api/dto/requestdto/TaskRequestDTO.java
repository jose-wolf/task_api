package com.josewolf.task_api.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequestDTO(
    @NotBlank(message = "A tarefa deve ter um título")
    String title,

    @NotBlank(message = "A descrição deve conter algum conteúdo")
    String description,

    @NotNull(message = "O ID do usuário é obrigatório")
    Long userId
) {
}
