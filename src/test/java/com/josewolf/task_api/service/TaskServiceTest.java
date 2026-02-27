package com.josewolf.task_api.service;

import com.josewolf.task_api.dto.TaskStatusRequestDTO;
import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.model.Task;
import com.josewolf.task_api.model.TaskStatus;
import com.josewolf.task_api.model.User;
import com.josewolf.task_api.repository.TaskRepository;
import com.josewolf.task_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso se o usuário existir.")
    void createTask_Success() {
        User userActual = new User();
        userActual.setId(1L);


        TaskRequestDTO request = new TaskRequestDTO("Estudar Mockito", "Praticar testes com múltiplos mocks", userActual.getId());
        Task task = new Task();
        task.setId(1L);
        task.setTitle(request.title());
        task.setUser(userActual);

        when(userRepository.findById(userActual.getId())).thenReturn(Optional.of(userActual));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponseDTO response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Estudar Mockito", response.title());
        verify(userRepository,times(1)).findById(userActual.getId());
        verify(taskRepository,times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se não achar o id do usuário.")
    void createTask_ThrowsException_WhenUsernameIdNotFound() {
        User userActual = new User();
        userActual.setId(999L);

        TaskRequestDTO request = new TaskRequestDTO("Estudar Mockito", "Praticar testes com múltiplos mocks", userActual.getId());

        when(userRepository.findById(userActual.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception =  assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(request));

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    //Lista
    @Test
    @DisplayName("Deve listar todas as tarefas com o seu usuário.")
    void findAllTasks_Success() {
        User user1 = new User();
        user1.setId(1L);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Estudar Mockito");
        task1.setUser(user1);

        User user2 = new User();
        user2.setId(2L);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Praticar testes.");
        task2.setUser(user2);

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        List<TaskResponseDTO> responseDTOS = taskService.listAllTasks();

        assertNotNull(responseDTOS);
        assertEquals(2, responseDTOS.size());
        assertEquals("Estudar Mockito", responseDTOS.get(0).title());
        assertEquals("Praticar testes.", responseDTOS.get(1).title());
        verify(taskRepository,times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar a tarefa comk o id do usuário.")
    void listTaskByUserId_Success() {
        User user1 = new User();
        user1.setId(1L);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Estudar Mockito");
        task1.setUser(user1);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Praticar testes.");
        task2.setUser(user1);

        when(userRepository.existsById(user1.getId())).thenReturn(true);
        when(taskRepository.findByUserId(user1.getId())).thenReturn(List.of(task1, task2));

        List<TaskResponseDTO> responseDTOS = taskService.listTasksByUserId(user1.getId());

        assertNotNull(responseDTOS);
        assertEquals(2, responseDTOS.size());
        assertEquals(user1.getId(), responseDTOS.get(0).userId());
        assertEquals(user1.getId(), responseDTOS.get(1).userId());
        assertEquals("Estudar Mockito", responseDTOS.get(0).title());
        assertEquals("Praticar testes.", responseDTOS.get(1).title());

        verify(taskRepository, times(1)).findByUserId(user1.getId());
        verify(userRepository, times(1)).existsById(user1.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção se não o usuário não existir")
    void listTaskByUserId_ThrowsException_WhenUsernameIdNotFound() {
        User user1 = new User();
        user1.setId(999L);

        when(userRepository.existsById(user1.getId())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () ->  taskService.listTasksByUserId(user1.getId()));

        assertEquals("Usuário não encontrado com o Id: 999", exception.getMessage());

        verify(taskRepository,never()).existsById(anyLong());
        verify(userRepository, times(1)).existsById(user1.getId());

    }

    @Test
    @DisplayName("Deve lançar exceção se o usuário existir, mas não existir tarefas.")
    void listTaskByUserId_ThrowsException_WhenUsernameExistsButTaskNotExist() {
        User user1 = new User();
        user1.setId(1L);

        when(userRepository.existsById(user1.getId())).thenReturn(true);
        when(taskRepository.findByUserId(user1.getId())).thenReturn(List.of());

        List<TaskResponseDTO> responseDTOS = taskService.listTasksByUserId(user1.getId());

        assertNotNull(responseDTOS);
        assertTrue(responseDTOS.isEmpty());
        assertEquals(0, responseDTOS.size());

        verify(userRepository, times(1)).existsById(user1.getId());
        verify(taskRepository, times(1)).findByUserId(user1.getId());
    }

    //update
    @Test
    @DisplayName("Deve atualzar a tarefa pelo id do usuário.")
    void updateTaskByUserId_Success() {
        Long taskId = 1L;

        Task taskOriginal = new Task();
        taskOriginal.setId(taskId);
        taskOriginal.setTitle("Título Antigo");
        taskOriginal.setDescription("Descrição Antiga");

        TaskRequestDTO requestDTO = new TaskRequestDTO("Título novo", "Descrição nova", taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOriginal));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO responseDTO = taskService.updateTask(taskId, requestDTO);

        assertNotNull(responseDTO);
        assertEquals("Título novo", responseDTO.title());
        assertEquals("Descrição nova",  responseDTO.description());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se o id da task não for encontrado.")
    void updateTaskByUserId_ThrowsException_WhenTaskIdNotFound() {
        Long taskId = 999L;

        TaskRequestDTO requestDTO = new TaskRequestDTO("Título novo", "Descrição nova", taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =  assertThrows(ResourceNotFoundException.class,
                () ->  taskService.updateTask(taskId, requestDTO));

        assertEquals("Task inexistente com o Id: 999", exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Deve atualizar apenas o status da tarefa com sucesso")
    void updateStatus_Success() {
        Long taskId = 1L;
        Task taskOriginal = new Task();
        taskOriginal.setId(taskId);
        taskOriginal.setTaskStatus(TaskStatus.PENDING);

        TaskStatusRequestDTO statusDTO = new TaskStatusRequestDTO(TaskStatus.COMPLETED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOriginal));
        when(taskRepository.save(any(Task.class))).thenReturn(taskOriginal);

        TaskResponseDTO responseDTO = taskService.updateStatus(taskId, statusDTO);

        assertEquals(TaskStatus.COMPLETED, responseDTO.taskStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository,times(1)).save(taskOriginal);
    }

    @Test
    @DisplayName("Deve lançar exceção se o id da task não for encontrado.")
    void updateTaskStatus_ThrowsException_WhenTaskIdNotFound() {
        Long taskId = 999L;

        TaskRequestDTO requestDTO = new TaskRequestDTO("Título novo", "Descrição nova", taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =  assertThrows(ResourceNotFoundException.class,
                () ->  taskService.updateTask(taskId, requestDTO));

        assertEquals("Task inexistente com o Id: 999", exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Deve deletar a task com sucesso.")
    void deleteTask_Success() {
        Long taskId = 1L;
        Task taskOriginal = new Task();
        taskOriginal.setId(taskId);
        taskOriginal.setTitle("Delete task");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOriginal));

        taskService.deleteTask(taskId);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).delete(taskOriginal);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar tarefa inexistente.")
    void deleteTask_ThrowsException_WhenTaskIdNotFound() {
        Long taskId = 999L;
        Task taskOriginal = new Task();
        taskOriginal.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(taskId);
        });

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).delete(taskOriginal);
    }

}


