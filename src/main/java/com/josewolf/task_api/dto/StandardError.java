package com.josewolf.task_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StandardError(
        @Schema(description = "Código de status HTTP", example = "404")
        Integer status,
        @Schema(description = "Motivo do erro", example = "Não encontrado")
        String error,
        @Schema(description = "Mensagem mais detalhada da exceção lançada", example = "O usuário não foi encontrado pelo id solicitado.")
        String message
) {
}
