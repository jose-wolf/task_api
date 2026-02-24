package com.josewolf.task_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TaskUpdateDTO(
        @Schema(description = "Nome do título para atualização", example = "Fazendo atividade (muda para) Fazer atividade")
        String title,

        @Schema(description = "Texto da descrição para atualização", example = "Fazendo atividade 1,2,3 (muda para) Fazer atividade 1 e 2")
        String description
) {

}
