package com.josewolf.task_api.service;

import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.model.Task;
import com.josewolf.task_api.model.TaskStatus;
import com.josewolf.task_api.model.User;
import com.josewolf.task_api.repository.TaskRepository;
import com.josewolf.task_api.repository.UserRepository;
import org.springframework.stereotype.Service;

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
}
