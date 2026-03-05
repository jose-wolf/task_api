package com.josewolf.task_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josewolf.task_api.dto.TaskStatusRequestDTO;
import com.josewolf.task_api.dto.requestdto.TaskRequestDTO;
import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.model.TaskStatus;
import com.josewolf.task_api.repository.TaskRepository;
import com.josewolf.task_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository  userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long criarUtilizadorEObterId() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

         return objectMapper.readTree(response).get("id").longValue();
    }


    //create
    @Test
    @DisplayName("Deve criar uma tarefa e vincular com um usuário existente e retornar status 201")
    void createTask_IntegrationSuccess() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Teste"))
                .andExpect(jsonPath("$.description").value("teste"))
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.PENDING.toString()));

        assertEquals(1, taskRepository.count());
        var taskNoDb = taskRepository.findAll().get(0);
        assertEquals(userId, taskNoDb.getUser().getId());
    }

    @Test
    @DisplayName("Deve retornar status 404 se o usuário não existe")
    void createTask_IntegrationNotFound() throws Exception {
        Long userId = 999L;

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, taskRepository.count());
    }

    @Test
    @DisplayName("Deve retornar status 400 quando a descrição está vazia")
    void createTask_IntegrationBadRequest_WhenDescriptionIsBlank() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, taskRepository.count());
    }

    @Test
    @DisplayName("Deve retornar status 400 quando o título está vazio")
    void createTask_IntegrationBadRequest_WhenTitleIsBlank() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, taskRepository.count());
    }

    //read
    @Test
    @DisplayName("Deve listar todas as tarefas existentes e retornar status 200")
    void getAllTask_IntegrationSuccess() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        TaskRequestDTO requestDTO2 = new TaskRequestDTO("Teste2", "teste2", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].userId").value(requestDTO.userId()))
                .andExpect(jsonPath("$[1].userId").value(requestDTO2.userId()))
                .andExpect(jsonPath("$[0].title").value(requestDTO.title()))
                .andExpect(jsonPath("$[1].title").value(requestDTO2.title()))
                .andExpect(jsonPath("$[0].description").value(requestDTO.description()))
                .andExpect(jsonPath("$[1].description").value(requestDTO2.description()));

        assertEquals(2, taskRepository.count());
    }

    @Test
    @DisplayName("Deve retornar status 200 para uma lista vazia")
    void getAllTasks_IntegrationSuccess_WhenListIsEmpty() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0));

        assertEquals(0, taskRepository.count(), "O banco de dados deveria estar vazio");
    }

    @Test
    @DisplayName("Lista todas as tasks pelo id do usuário e retorna status 200")
    void getAllTasks_IntegrationSuccess_WhenFindByUserId() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        TaskRequestDTO requestDTO2 = new TaskRequestDTO("Teste2", "teste2", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].userId").value(requestDTO.userId()))
                .andExpect(jsonPath("$[1].userId").value(requestDTO2.userId()))
                .andExpect(jsonPath("$[0].title").value(requestDTO.title()))
                .andExpect(jsonPath("$[1].title").value(requestDTO2.title()))
                .andExpect(jsonPath("$[0].description").value(requestDTO.description()))
                .andExpect(jsonPath("$[1].description").value(requestDTO2.description()));

        assertEquals(2, taskRepository.count());
    }

    @Test
    @DisplayName("Deve retornar status 404 se o usuário não existir")
    void getAllTasksById_IntegrationFailed_WhenNotFoundUserId() throws Exception {
        Long userId  = 999L;

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, taskRepository.count(), "Não deve existir tarefas, porque usuário não existe");
    }

    //update
    @Test
    @DisplayName("Deve atualizar os campos e retornar status 200")
    void updateTaskByTaskId_IntegrationSuccess() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        String responseJson = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseJson).get("id").asLong();

        TaskRequestDTO newRequestDTO = new TaskRequestDTO("Novo teste", "Nova descrição",  userId);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequestDTO)))
                .andExpect(status().isOk());

        assertEquals(1,taskRepository.count(), "A tarefa deve ter sido atualizada");
    }

    @Test
    @DisplayName("Deve retornar status 400 se um dos campos não forem preenchidos")
    void updateTaskByTaskId_IntegrationFailed_WhenDesciptionIsBlank() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        String responseJson = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseJson).get("id").asLong();

        TaskRequestDTO newRequestDTO = new TaskRequestDTO("Novo teste", "",  userId);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequestDTO)))
                .andExpect(status().isBadRequest());

        assertEquals(1,taskRepository.count(), "A tarefa não deve ser atualizada");
    }

    @Test
    @DisplayName("Deve retornar status 404 se não for encontrado a tarefa")
    void updateTaskByTaskId_IntegrationFailed_WhenNotFoundTaskId() throws Exception {
        Long userId  = criarUtilizadorEObterId();
        Long taskId = 999L;

        TaskRequestDTO updateRequest = new TaskRequestDTO("Novo Titulo", "Nova Descrição", userId);

        mockMvc.perform(put("/api/tasks/" +  taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(0, taskRepository.count(), "Não deveria haver tarefas no banco");
    }

    //patch
    @Test
    @DisplayName("Deve atualizar o status da task e retornar status 200")
    void updateTaskStatusByTaskId_IntegrationSuccess() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        String responseJson = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseJson).get("id").asLong();

        TaskStatusRequestDTO taskStatusRequestDTO = new TaskStatusRequestDTO(TaskStatus.COMPLETED);

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.toString()));

    }

    @Test
    @DisplayName("Deve retornar status 400 se a mudança de status não ocorrer")
    void updateTaskStatusByTaskId_IntegrationFailed() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        String responseJson = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseJson).get("id").asLong();

        TaskStatusRequestDTO taskStatusRequestDTO = new TaskStatusRequestDTO(null);

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar status 404 se não for encontrado a tarefa")
    void updateTaskStatusByTaskId_IntegrationFailed_WhenNotFoundTaskId() throws Exception {
        Long userId  = criarUtilizadorEObterId();
        Long taskId = 999L;

        TaskRequestDTO updateRequest = new TaskRequestDTO("Novo Titulo", "Nova Descrição", userId);

        TaskStatusRequestDTO taskStatusRequestDTO = new TaskStatusRequestDTO(TaskStatus.COMPLETED);

        mockMvc.perform(patch("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    //Delete

    @Test
    @DisplayName("Deve deletar a task e retornar status 204")
    void deletaTaskByTaskId_IntegrationSuccess() throws Exception {
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        String responseJson = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        assertEquals(0, taskRepository.count(), "A tarefa deve ser deletada");
    }

    @Test
    @DisplayName("Deve retornar status 404 se não for encontrado a task com o id")
    void deleteTaskByTaskId_IntegrationNotFound_WhenIdDoesNotExist() throws Exception {
        Long taskId = 999L;
        Long userId  = criarUtilizadorEObterId();

        TaskRequestDTO requestDTO = new TaskRequestDTO("Teste", "teste", userId);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(1, taskRepository.count(), "A tarefa deve ser deletada");
    }
}
