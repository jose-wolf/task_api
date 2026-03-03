package com.josewolf.task_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.dto.responsedto.UserResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //create
    @Test
    @DisplayName("Deve retornar 201 Created ao criar um usuário")
    void createUser_ReturnsCreated() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");
        UserResponseDTO responseDTO = new UserResponseDTO(1l, "Teste", "teste@gmail.com");

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Teste"))
                .andExpect(jsonPath("$.email").value("teste@gmail.com"));
    }

    @Test
    @DisplayName("Deve lançar exceção se email estiver nulo")
    void createUser_ReturnsBadRequest() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se username estiver nulo")
    void createUser_ReturnsBadRequest_WhenUsernameIsNull() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO(null, "teste@gmail.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    //REad

    @Test
    @DisplayName("Deve retornar 200 OK e os dados do usuário ao buscar por username existente")
    void getUserByUsername_ReturnOk_WhenUsernameExist() throws Exception {
        Long userId = 1L;
        String username = "Teste";
        UserResponseDTO responseDTO = new UserResponseDTO(userId,username, "teste98@gmail.com");

        when(userService.findByUsername(username)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/search")
                .param("username", responseDTO.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(responseDTO.username()));
    }

    @Test
    @DisplayName("Deve retornar 200 OK e os dados do usuário ao buscar por email existente")
    void getUserByEmail_ReturnOk_WhenEmailExist() throws Exception {
        Long userId = 1L;
        String email = "teste98@gmail.com";
        UserResponseDTO responseDTO = new UserResponseDTO(userId,"Teste", email);

        when(userService.findByEmail(email)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/search/email")
                .param("email", responseDTO.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(responseDTO.email()));
    }

    @Test
    @DisplayName("Deve retornar 200 OK e os dados dos usuários")
    void getUserByAll_ReturnOk_WhenAllUsersExist() throws Exception {
        UserResponseDTO responseDTO1 = new UserResponseDTO(1L,"Teste1", "teste98@gmail.com");
        UserResponseDTO responseDTO2 = new UserResponseDTO(2L,"Teste2", "teste98@gmail.com");

        when(userService.findAllUsers()).thenReturn(List.of(responseDTO1, responseDTO2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].username").value(responseDTO1.username()))
                .andExpect(jsonPath("$[1].username").value(responseDTO2.username()))
                .andExpect(jsonPath("$[0].email").value(responseDTO1.email()))
                .andExpect(jsonPath("$[1].email").value(responseDTO2.email()));
    }

    @Test
    @DisplayName("Deve retornar 404 not found se a busca for realizada por username")
    void getUserByUsername_ReturnNotFound_WhenUsernameNotExist() throws Exception {
        String username = "Teste inexistente";

        when(userService.findByUsername(username)).thenThrow(new ResourceNotFoundException("Usuário não encontrado com o nome: Teste inexistente"));

        mockMvc.perform(get("/api/users/search")
                        .param("username", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 404 not found se a busca for realizada por email")
    void getUserByEmail_ReturnNotFound_WhenEmailNotExist() throws Exception {
        String email = "teste_inexistente@gmail.com";

        when(userService.findByEmail(email)).thenThrow(new ResourceNotFoundException("Email não encontrado: teste_inexistente@gmail.com"));

        mockMvc.perform(get("/api/users/search/email")
                        .param("email", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 200 ok se não for encontrado usuários existentes")
    void getUserByAll_ReturnOk_WhenAllUsersNotExist() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(0));
    }

    //update

    @Test
    @DisplayName("Deve retornar um 200 ok se a alteração deu certo.")
    void updateUser_ReturnOk_WhenUserExists() throws Exception {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO("Novo Nome", "novo@email.com");
        UserResponseDTO response = new UserResponseDTO(userId, "Novo Nome", "novo@email.com");

        when(userService.updateUserById(eq(userId), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(request.username()))
                .andExpect(jsonPath("$.email").value(request.email()));

    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se o id não for encontrado")
    void updateUserById_ReturnNotFound_WhenIdNotExist() throws Exception {
        Long userId = 999L;
        UserRequestDTO request = new UserRequestDTO("Novo Nome", "novo@email.com");

        when(userService.updateUserById(eq(userId), any(UserRequestDTO.class))).thenThrow(new ResourceNotFoundException("Usuário não encontrado com o id: 999"));

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict caso já exista username sendo usado por outro usuário")
    void updateUser_ReturnDataIntegrityViolationException_WhenUsernameExists() throws Exception {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO("Novo Nome", "novo@email.com");

        when(userService.updateUserById(eq(userId), any(UserRequestDTO.class))).thenThrow(new DataIntegrityViolationException("O nome de usuário já está em uso."));

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict caso já exista email sendo usado por outro usuário")
    void updateUser_ReturnDataIntegrityViolationException_WhenEmailExists() throws Exception {
        Long userId = 1L;
        UserRequestDTO request = new UserRequestDTO("Novo Nome", "novo@email.com");

        when(userService.updateUserById(eq(userId), any(UserRequestDTO.class))).thenThrow(new DataIntegrityViolationException("O email já está em uso."));

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(409));
    }

    //delete
    @Test
    @DisplayName("Deve retornar 204 No Content quando o id for existente.")
    void deleteUserById_ReturnNoContent_WhenIdExist() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao tentar deletar usuário inexistente.")
    void deleteUserById_ReturnNotFound_WhenIdNotExist() throws Exception {
        Long userId = 999L;

        doThrow(new ResourceNotFoundException("Usuário não encontrado com o id: 999"))
                .when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }
}
