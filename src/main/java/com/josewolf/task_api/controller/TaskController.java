package com.josewolf.task_api.controller;

import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.model.Task;
import com.josewolf.task_api.repository.UserRepository;
import com.josewolf.task_api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskRequestDTO requestDTO) {
        TaskResponseDTO taskResponseDTO = taskService.createTask(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        List<TaskResponseDTO> taskResponseDTOS = taskService.listAllTasks();
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTOS);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUserId(@PathVariable Long userId) {
        List<TaskResponseDTO> taskResponseDTOS = taskService.listTasksByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponseDTOS);
    }
}
