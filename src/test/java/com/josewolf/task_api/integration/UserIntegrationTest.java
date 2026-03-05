package com.josewolf.task_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve salvar o usuário no banco de dados e retornar 201 Created.")
    void createUser_IntegrationSuccess() throws Exception {

        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Teste"))
                .andExpect(jsonPath("$.email").value("teste@gmail.com"));

        var userOptional = userRepository.findByEmail("teste@gmail.com");

        assertTrue(userOptional.isPresent(), "O usuário deveria ter sido persistido no H2");
        assertEquals("Teste", userOptional.get().getUsername());
    }

    @Test
    @DisplayName("Deve retornar 400 bad request ao tentar salvar usuário com email inválido")
    void createUser_IntegrationFailed_WhenEmailIsInvalid() throws Exception {

        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste", "");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        assertEquals(0, userRepository.count(), "O banco deveria estar vazio");
    }

    @Test
    @DisplayName("Deve retornar 400 bad request ao tentar salvar usuário com username inválido")
    void createUser_IntegrationFailed_WhenUsernameIsInvalid() throws Exception {

        UserRequestDTO userRequestDTO = new UserRequestDTO("", "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        var userOptional = userRepository.findByUsername("");

        assertTrue(userOptional.isEmpty(), "O banco deveria estar vazio");
        assertEquals(0, userRepository.count(), "Não deveria existir nenhum registro");
    }

    //Read

    @Test
    @DisplayName("Deve listar todos os usuários existente e retornar 200 Ok")
    void getAllUsers_IntegrationSuccess() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        UserRequestDTO requestDTO2 = new UserRequestDTO("Teste2", "teste@gmail.com2");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].username").value(requestDTO.username()))
                .andExpect(jsonPath("$[0].email").value(requestDTO.email()))
                .andExpect(jsonPath("$[1].username").value(requestDTO2.username()))
                .andExpect(jsonPath("$[1].email").value(requestDTO2.email()));
    }

    @Test
    @DisplayName("Deve buscar um usuário por username com sucesso no banco real")
    void getUserByUsername_IntegrationSuccess() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/users/search")
                        .param("username", requestDTO.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(requestDTO.username()))
                .andExpect(jsonPath("$.email").value(requestDTO.email()));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar username inexistente")
    void getUserByUsername_WhenUsernameIsInvalid() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("username", "invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve buscar um usuário por email com sucesso no banco real")
    void getUserByEmail_IntegrationSuccess() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/users/search/email")
                        .param("email", requestDTO.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(requestDTO.username()))
                .andExpect(jsonPath("$.email").value(requestDTO.email()));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar email inexistente")
    void getUserByEmail_WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(get("/api/users/search/email")
                        .param("email", "invalid@gmail.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.message").exists());
    }

    //update
    @Test
    @DisplayName("Deve retornar um 200 ok se a alteração for um sucesso")
    void updateUserById_WhenIdIsValid() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long idParaAtualizar = objectMapper.readTree(responseJson).get("id").asLong();

        UserRequestDTO newRequestDTO = new UserRequestDTO("Teste2", "teste@gmail.com");

        mockMvc.perform(patch("/api/users/" + idParaAtualizar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(newRequestDTO.username()))
                .andExpect(jsonPath("$.email").value(newRequestDTO.email()));
    }

    @Test
    @DisplayName("Deve retornar 404 not found se não for encontrado o Id do usuário")
    void updateUserById_IntegrationNotFound_WhenIdIsInvalid() throws Exception {
        Long  idInexistente = 100L;
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");


        mockMvc.perform(patch("/api/users/" + idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(0, userRepository.count(), "O usuário não deve existir.");

    }

    @Test
    @DisplayName("Deve retornar 409 ao colocar username que já está em uso")
    void updateUserById_IntegrationConflict_WhhenUsernameExistsInTheOtherUser() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long idParaAtualizar = objectMapper.readTree(responseJson).get("id").asLong();

        UserRequestDTO requestDTO2 = new UserRequestDTO("Teste2", "teste@gmail.com2");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(status().isCreated());

        UserRequestDTO updateRequestDTO = new UserRequestDTO("Teste2", "teste@gmail.com");


        mockMvc.perform(patch("/api/users/" + idParaAtualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Deve retornar 409 ao colocar email que já está em uso")
    void updateUserById_IntegrationConflict_WhhenEmailExistsInTheOtherUser() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        String responseJson = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long idParaAtualizar = objectMapper.readTree(responseJson).get("id").asLong();

        UserRequestDTO requestDTO2 = new UserRequestDTO("Teste2", "teste@gmail.com2");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(status().isCreated());

        UserRequestDTO updateRequestDTO = new UserRequestDTO("Teste", "teste@gmail.com2");


        mockMvc.perform(patch("/api/users/" + idParaAtualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    //Delete
    @Test
    @DisplayName("Deve deletar o usuário no banco de dados e retornar 204 no content")
    void deleteUser_IntegrationSuccess() throws Exception {

        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        String responseJson = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long idParaDeletar = objectMapper.readTree(responseJson).get("id").asLong();

        mockMvc.perform(delete("/api/users/" + idParaDeletar))
                        .andExpect(status().isNoContent());

        assertEquals(0, userRepository.count(), "O banco deveria estar vazio");
    }

    @Test
    @DisplayName("Deve retornar 404 not found se não for encontrado o Id do usuário")
    void deleteUser_IntegrationNotFound_WhenIdDoesNotExist() throws Exception {
        Long  idInexistente = 100L;

        mockMvc.perform(delete("/api/users/" + idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(0, userRepository.count(), "O banco deveria estar vazio");
    }


}
