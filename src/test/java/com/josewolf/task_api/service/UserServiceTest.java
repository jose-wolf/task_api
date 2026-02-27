package com.josewolf.task_api.service;

import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.dto.responsedto.UserResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.model.User;
import com.josewolf.task_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private  UserService userService;

    //Create
    @Test
    @DisplayName("Deve criar um usuário com sucesso.")
    void createdUser_Success(){
        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste", "teste@gmail.com");
        User user = new User();
        user.setId(1L);
        user.setUsername("Teste");
        user.setEmail("teste@gmail.com");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO responseDTO = userService.createUser(userRequestDTO);

        assertNotNull(responseDTO);
        assertEquals("Teste", responseDTO.username());
        assertEquals("teste@gmail.com", responseDTO.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já está cadastrado.")
    void createdUser_ThrowsException_WhenEmailAlreadyExists(){
        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(new User()));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () ->  userService.createUser(userRequestDTO));

        assertEquals("O e-mail 'teste@gmail.com' já pertence a outra conta.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o username já está cadastrado.")
    void createdUser_ThrowsException_WhenUsernameAlreadyExists(){
        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        when(userRepository.findByUsername("Teste")).thenReturn(Optional.of(new User()));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () ->  userService.createUser(userRequestDTO));

        assertEquals("O nome de usuário 'Teste' já está sendo utilizado.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    //Update
    @Test
    @DisplayName("Deve atualizar somente o email com sucesso.")
    void updateUser_EmailSuccess(){
        Long userId = 1L;
        User userActual = new User();
        userActual.setId(userId);
        userActual.setUsername("Teste");
        userActual.setEmail("teste0123456789@gmail.com");

        UserRequestDTO userRequestDTO = new UserRequestDTO(null, "teste@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userActual));
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userActual);

        UserResponseDTO userResponseDTO = userService.updateUserById(userId, userRequestDTO);

        assertNotNull(userResponseDTO);
        assertEquals("teste@gmail.com", userResponseDTO.email());
        assertEquals("Teste", userResponseDTO.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar somente o username com sucesso.")
    void updateUser_UsernameSuccess(){
        Long userId = 1L;
        User userActual = new User();
        userActual.setId(userId);
        userActual.setUsername("Teste");
        userActual.setEmail("teste0123456789@gmail.com");

        UserRequestDTO userRequestDTO = new UserRequestDTO("Teste2", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userActual));
        when(userRepository.findByUsername("Teste2")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userActual);

        UserResponseDTO userResponseDTO = userService.updateUserById(userId, userRequestDTO);

        assertNotNull(userResponseDTO);
        assertEquals("teste0123456789@gmail.com", userResponseDTO.email());
        assertEquals("Teste2", userResponseDTO.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando atualizar e já tiver username existente por outro.")
    void updateUser_ThrowsException_WhenUsernameExistent() {
        Long userId = 1L;
        Long otherUserId = 2L;

        User userActual = new User();
        userActual.setId(userId);
        userActual.setUsername("nomeAntigo");

        User usuarioConflitante = new User();
        usuarioConflitante.setId(otherUserId);
        usuarioConflitante.setUsername("TesteUsername");

        UserRequestDTO requestDTO = new UserRequestDTO("TesteUsername", "teste@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userActual));
        when(userRepository.findByUsername("TesteUsername")).thenReturn(Optional.of(usuarioConflitante));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> userService.updateUserById(userId, requestDTO));

        assertEquals("O nome de usuário já está em uso.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando atualizar e já tiver username existente por outro.")
    void updateUser_ThrowsException_WhenEmailExistent() {
        Long userId = 1L;
        Long otherUserId = 2L;

        User userActual = new User();
        userActual.setId(userId);
        userActual.setUsername("nomeAntigo");
        userActual.setEmail("gmailAntigo@gmail.com");

        User usuarioConflitante = new User();
        usuarioConflitante.setId(otherUserId);
        usuarioConflitante.setUsername("TesteUsername");
        usuarioConflitante.setEmail("teste@gmail.com");

        UserRequestDTO requestDTO = new UserRequestDTO("TesteUsername", "teste@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userActual));
        when(userRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(usuarioConflitante));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> userService.updateUserById(userId, requestDTO));

        assertEquals("O email já está em uso.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando não achar o id.")
    void updateUser_ThrowsException_WhenIdNotFound() {
        Long userId = 999L;
        UserRequestDTO requestDTO = new UserRequestDTO("Teste", "teste@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserById(userId, requestDTO);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    //list
    @Test
    @DisplayName("Deve listar com sucesso todos os usuários.")
    void findAllUsers_Success() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("teste1");
        user1.setEmail("teste1@gmail.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("teste2");
        user2.setEmail("teste2@gmail.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> userResponseDTOs = userService.findAllUsers();

        assertNotNull(userResponseDTOs);
        assertEquals(2, userResponseDTOs.size());
        assertEquals("teste1", userResponseDTOs.get(0).username());
        assertEquals("teste2", userResponseDTOs.get(1).username());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar o usuário que possuir o username igual ao inserido.")
    void findByUsername_Success(){
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("teste1");
        user1.setEmail("teste1@gmail.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("teste2");
        user2.setEmail("teste2@gmail.com");

        when(userRepository.findByUsername("teste2")).thenReturn(Optional.of(user2));

        UserResponseDTO userResponseDTO = userService.findByUsername("teste2");

        assertNotNull(userResponseDTO);
        assertEquals("teste2", userResponseDTO.username());
        verify(userRepository, times(1)).findByUsername("teste2");
    }

    @Test
    @DisplayName("Deve buscar o usuário que possuir o email igual ao inserido.")
    void findByEmail_Success(){
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("teste1");
        user1.setEmail("teste1@gmail.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("teste2");
        user2.setEmail("teste2@gmail.com");

        when(userRepository.findByEmail("teste1@gmail.com")).thenReturn(Optional.of(user1));

        UserResponseDTO userResponseDTO = userService.findByEmail("teste1@gmail.com");

        assertNotNull(userResponseDTO);
        assertEquals("teste1@gmail.com", userResponseDTO.email());
        verify(userRepository, times(1)).findByEmail("teste1@gmail.com");
    }

    @Test
    @DisplayName("Deve lançar excessão se o nome não for encontrado")
    void findByUsername_ThrowsException_WhenUsernameNotFound() {
        String usernameTeste = "Teste";

        when(userRepository.findByUsername(usernameTeste)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findByUsername(usernameTeste);
        });

        verify(userRepository, times(1)).findByUsername(usernameTeste);
    }

    @Test
    @DisplayName("Deve lançar excessão se o email não for encontrado")
    void findByUsername_ThrowsException_WhenEmailNotFound() {
        String emailTeste = "teste@gmail.com";

        when(userRepository.findByEmail(emailTeste)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findByEmail(emailTeste);
        });

        verify(userRepository, times(1)).findByEmail(emailTeste);
    }

    @Test
    @DisplayName("Deve deletar com sucesso")
    void deleteUser_Success(){
        Long userId = 1L;
        User userActual = new User();
        userActual.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userActual));

        userService.deleteUserById(userId);
        verify(userRepository, times(1)).delete(userActual);
    }

    @Test
    @DisplayName("Deve deletar com sucesso")
    void deleteUser_ThrowsException_WhenUserIdNotFound() {
        Long userId = 999L;
        User userActual = new User();
        userActual.setId(userId);

        when(userRepository.findById(userActual.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUserById(userId);
        });

        verify(userRepository, never()).delete(userActual);
    }

}
