package com.josewolf.task_api.dto.responsedto;

import com.josewolf.task_api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record TaskResponseDTO(

    @Schema(description = "Id único gerado pelo banco de dados", example = "1")
    Long id,
    @Schema(description = "Retorna o título", example = "Ler livro.")
    String title,
    @Schema(description = "Retorna a descrição", example = "Continuar leitura do livro.")
    String description,
    @Schema(description = "Status atual da tarefa", example = "PENDING")
    TaskStatus taskStatus,
    @Schema(description = "Retorna o ID do usuário relacionado com a task.", example = "task = 5, pertence ao usuário = 7")
    Long userId

) { }
