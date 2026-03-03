package com.josewolf.task_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josewolf.task_api.dto.TaskStatusRequestDTO;
import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.responsedto.TaskResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.model.TaskStatus;
import com.josewolf.task_api.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve retornar 201 Created ao criar uma tarefa com sucesso")
    void createTask_ReturnsCreated() throws Exception {

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste title", "teste description", 1L);
        TaskResponseDTO responseDTO = new TaskResponseDTO(1L,"Teste title", "teste description", TaskStatus.PENDING, 1l);

        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDTO.id()))
                .andExpect(jsonPath("$.title").value(responseDTO.title()))
                .andExpect(jsonPath("$.description").value(responseDTO.description()))
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$.userId").value(responseDTO.userId()));
    }

    @Test
    @DisplayName("Deve lançar 404 Not Found se não encontrado o usuário")
    void createTask_ReturnsNotFound_WhenUserIdNotFound() throws Exception {
        Long userId = 999L;
        TaskRequestDTO request = new TaskRequestDTO("Título", "Descrição", userId);

        when(taskService.createTask(any(TaskRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve lançar 400 Bad Request se não possuir titulo ou descrição")
    void createTask_ReturnsBadRequest_WhenTitleNotExist() throws Exception {
        TaskRequestDTO requestDTO = new TaskRequestDTO(null, "teste description", 1L);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    //read

    @Test
    @DisplayName("Deve retornar 200 ok e listar todas as tarefas")
    void getTask_ReturnsTask() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO(1L,"Teste title", "teste description", TaskStatus.PENDING, 1l);
        TaskResponseDTO responseDTO2 = new TaskResponseDTO(2L,"Teste title2", "teste description2", TaskStatus.PENDING, 1l);

        when(taskService.listAllTasks()).thenReturn(List.of(responseDTO, responseDTO2));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(responseDTO.id()))
                .andExpect(jsonPath("$[0].title").value(responseDTO.title()))
                .andExpect(jsonPath("$[0].description").value(responseDTO.description()))
                .andExpect(jsonPath("$[0].taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$[0].userId").value(responseDTO.userId()))
                .andExpect(jsonPath("$[1].id").value(responseDTO2.id()))
                .andExpect(jsonPath("$[1].title").value(responseDTO2.title()))
                .andExpect(jsonPath("$[1].description").value(responseDTO2.description()))
                .andExpect(jsonPath("$[1].taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$[1].userId").value(responseDTO2.userId()));


    }

    @Test
    @DisplayName("Deve retornar 200 Ok e uma lista vazia")
    void getTasks_ReturnsEmptyList_WhenListIsEmpty() throws Exception {
        when(taskService.listAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @DisplayName("Retorna 200 ok e lista todas as tarefas por id do usuário")
    void getTasksByUserId_ReturnsOk() throws Exception {
        Long userId = 1L;
        TaskResponseDTO responseDTO = new TaskResponseDTO(1L,"Teste title", "teste description", TaskStatus.PENDING, userId);
        TaskResponseDTO responseDTO2 = new TaskResponseDTO(2L,"Teste title2", "teste description2", TaskStatus.PENDING, userId);

        when(taskService.listTasksByUserId(responseDTO.userId())).thenReturn(List.of(responseDTO, responseDTO2));

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(responseDTO.id()))
                .andExpect(jsonPath("$[0].title").value(responseDTO.title()))
                .andExpect(jsonPath("$[0].description").value(responseDTO.description()))
                .andExpect(jsonPath("$[0].taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$[0].userId").value(responseDTO2.userId()))
                .andExpect(jsonPath("$[1].id").value(responseDTO2.id()))
                .andExpect(jsonPath("$[1].title").value(responseDTO2.title()))
                .andExpect(jsonPath("$[1].description").value(responseDTO2.description()))
                .andExpect(jsonPath("$[1].taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$[1].userId").value(responseDTO2.userId()));

    }

    @Test
    @DisplayName("Retorna 404 Not Found se o id do usuário não for encontrado")
    void getTasksByUserId_ReturnsNotFound_WhenIdDoesNotExist() throws Exception {
        Long userId = 999L;
        TaskResponseDTO responseDTO = new TaskResponseDTO(1L,"Teste title", "teste description", TaskStatus.PENDING, userId);

        when(taskService.listTasksByUserId(responseDTO.userId()))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado com o Id: 999"));

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());;
    }

    //update - PUT

    @Test
    @DisplayName("Retorna 200 Ok, atualizando titulo e descrição")
    void updateTaskByTaskId_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long taskId = 1L;

        TaskRequestDTO requestDTO = new TaskRequestDTO("Titulo novo", "Descrição nova", userId);
        TaskResponseDTO responseDTO = new TaskResponseDTO(taskId, "Titulo novo", "Descrição nova", TaskStatus.PENDING, userId);

        when(taskService.updateTask(taskId, requestDTO)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(responseDTO.title()))
                .andExpect(jsonPath("$.description").value(responseDTO.description()))
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.PENDING.toString()))
                .andExpect(jsonPath("$.userId").value(responseDTO.userId()))
                .andExpect(jsonPath("$.id").value(responseDTO.id()));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se a tarefa não existir")
    void updateTaskByTaskId_ReturnsNotFound_WhenIdDoesNotExist() throws Exception {
        Long userId = 1L;
        Long taskId = 999L;

        TaskRequestDTO requestDTO = new TaskRequestDTO("Titulo novo", "Descrição nova", userId);

        when(taskService.updateTask(taskId, requestDTO)).thenThrow(new ResourceNotFoundException("Task inexistente com o Id: 999"));

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());;

        verify(taskService, times(1)).updateTask(taskId, requestDTO);
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se não possuir titulo e descrição")
    void updateTaskByTaskId_ReturnsBadRequest_WhenTituloIsEmpty() throws Exception {
        Long userId = 1L;
        Long  taskId = 1L;

        TaskRequestDTO requestDTO = new TaskRequestDTO(null, "Descrição nova", userId);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    //Update -  Patch
    @Test
    @DisplayName("Deve retornar 200 Ok ao atualizar o staus da tarefa")
    void updateTaskStatusByTaskId_ReturnsOk() throws Exception {
        Long taskId =  1L;

        TaskStatusRequestDTO statusRequestDTO = new TaskStatusRequestDTO(TaskStatus.COMPLETED);
        TaskResponseDTO responseDTO = new TaskResponseDTO(taskId, "Título", "Descrição" , TaskStatus.COMPLETED, 1L);

        when(taskService.updateStatus(eq(taskId), any(TaskStatusRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.toString()));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se a tarefa não existir")
    void updateTaskStatusByTaskId_ReturnsNotFound_WhenIdDoesNotExist() throws Exception {
        Long taskId = 999L;

        TaskStatusRequestDTO statusRequestDTO = new TaskStatusRequestDTO(TaskStatus.COMPLETED);

        when(taskService.updateStatus(eq(taskId), any(TaskStatusRequestDTO.class))).thenThrow(new ResourceNotFoundException("Task inexistente com o Id: 999"));

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(taskService, times(1)).updateStatus(taskId, statusRequestDTO);
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se o campo não for inserido")
    void updateTaskStatus_ReturnsBadRequest_WhenStatusIsNull() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)) // Sem o .content()
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateStatus(any(), any());
    }

    //Delete
    @Test
    @DisplayName("Deve retornar 204 No Content ao deletar uma tarefa existente")
    void deleteTask_ReturnsNoContent() throws Exception {
        Long taskId = 1L;

        doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se não for encontrado o id da task")
    void deleteTask_ReturnsNotFound_WhenIdDoesNotExist() throws Exception {
        Long taskId = 999L;

        doThrow(new ResourceNotFoundException("Task inexistente com o Id: 999")).when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).deleteTask(taskId);
    }
}
