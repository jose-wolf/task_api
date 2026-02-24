package com.josewolf.task_api.controller;

import com.josewolf.task_api.dto.TaskStatusRequestDTO;
import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tasks", description = "Responsável por ações da task.")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Cria uma task", description = "Retorna uma task criada relacionada com o usuário por id.")
    @ApiResponse(responseCode = "201", description = "Cria uma task relacionada com o id do usuário.")
    @ApiResponse(responseCode = "400", description = "O corpo da requisição está ausente ou possui campos inválidos.")
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO requestDTO) {
        TaskResponseDTO taskResponseDTO = taskService.createTask(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponseDTO);
    }

    @GetMapping
    @Operation(summary = "Lista todas as task", description = "Retorna lista de todas as tasks.")
    @ApiResponse(responseCode = "200", description = "Lista todas as tasks existentes.")
    @ApiResponse(responseCode = "404", description = "Não existem tasks para serem listadas.")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        List<TaskResponseDTO> taskResponseDTOS = taskService.listAllTasks();
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTOS);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lista as tasks", description = "Lista todas as tasks, que o usuário possui por id.")
    @ApiResponse(responseCode = "200", description = "Task encontrada com sucesso pelo id do usuário.")
    @ApiResponse(responseCode = "404", description = "Não existe usuário com o id solicitado.")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUserId(@PathVariable Long userId) {
        List<TaskResponseDTO> taskResponseDTOS = taskService.listTasksByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTOS);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza a task", description = "Retorna uma ataulização do titulo e da descrição da task pelo id.")
    @ApiResponse(responseCode = "200", description = "Task encontrada pelo id, e atualizada com sucesso.")
    @ApiResponse(responseCode = "400", description = "O corpo da requisição está ausente ou possui campos inválidos.")
    @ApiResponse(responseCode = "404", description = "Não existe task com o id solicitado.")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody TaskRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.updateTask(id, requestDTO));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status da Task", description = "Retorna o status da task atualizado.")
    @ApiResponse(responseCode = "200", description = "Task encontrada pelo id, e status atualizado com sucesso.")
    @ApiResponse(responseCode = "400", description = "O corpo da requisição está ausente ou possui campos inválidos.")
    @ApiResponse(responseCode = "404", description = "Não existe task com o id solicitado.")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(@PathVariable Long id, @RequestBody TaskStatusRequestDTO statusRequestDTO) {
        return ResponseEntity.ok(taskService.updateStatus(id,statusRequestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta uma task", description = "Excluí uma task por id")
    @ApiResponse(responseCode = "204", description = "Task deletada com sucesso.")
    @ApiResponse(responseCode = "404", description = "Não existe task com o id solicitado.")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
