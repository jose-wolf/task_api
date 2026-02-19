package com.josewolf.task_api.service;

import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.model.Task;
import com.josewolf.task_api.model.TaskStatus;
import com.josewolf.task_api.model.User;
import com.josewolf.task_api.repository.TaskRepository;
import com.josewolf.task_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;

    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {

        User user = userRepository.findById(taskRequestDTO.userId()).orElseThrow(
                () -> new RuntimeException("Usuário não encontrado"));

        Task task = new Task();

        task.setTitle(taskRequestDTO.title());
        task.setDescription(taskRequestDTO.description());
        task.setTaskStatus(TaskStatus.PENDING);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        return new TaskResponseDTO(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getTaskStatus(),
                savedTask.getUser().getId()
        );
    }

    public List<TaskResponseDTO> listAllTasks() {

        List<Task> tasks = taskRepository.findAll();

        if(tasks.isEmpty()) {
            throw  new ResourceNotFoundException("Nenhuma Task encontrada no sistema");
        }

        return tasks.stream()
                .map(task -> {
                    return new TaskResponseDTO(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getTaskStatus(),
                            task.getUser() != null ? task.getUser().getId() : null
                    );
                }).toList();
    }

    public List<TaskResponseDTO> listTasksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado com o Id: " + userId);
        }

        List<Task> tasks = taskRepository.findByUserId(userId);

        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma Task encontrada no sistema");
        }

        return tasks.stream()
                .map(task -> new TaskResponseDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getTaskStatus(),
                        task.getUser() != null ? task.getUser().getId() : null
                )).toList();
    }
}
