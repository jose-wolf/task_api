package com.josewolf.task_api.service;

import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.dto.responsedto.UserResponseDTO;
import com.josewolf.task_api.exceptions.ResourceNotFoundException;
import com.josewolf.task_api.model.User;
import com.josewolf.task_api.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {


    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        if (userRepository.findByUsername(userRequestDTO.username()).isPresent()) {
            throw new DataIntegrityViolationException("O nome de usuário '" + userRequestDTO.username() + "' já está sendo utilizado.");
        }

        if (userRepository.findByEmail(userRequestDTO.email()).isPresent()) {
            throw new DataIntegrityViolationException("O e-mail '" + userRequestDTO.email() + "' já pertence a outra conta.");
        }
        User user = new User();

        user.setUsername(userRequestDTO.username());
        user.setEmail(userRequestDTO.email());


        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();

        if(users.isEmpty()) {
            throw new ResourceNotFoundException("Usuários inexistentes.");
        }

        return users.stream()
                .map( user -> {
                    return new UserResponseDTO(
                      user.getId(),
                      user.getUsername(),
                      user.getEmail()
                    );
                        }).toList();
    }

    public UserResponseDTO findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Usuário não encontrado com o nome: " + username);
        }

        User user = userOptional.get();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public UserResponseDTO findByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Email não encontrado: " + email);
        }

        User user = userOptional.get();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public UserResponseDTO updateUserById(Long id, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o id: " + id));

        if (userRequestDTO.username() != null && !userRequestDTO.username().isBlank()) {
            userRepository.findByUsername(userRequestDTO.username())
                            .ifPresent(existingUserName -> {
                                if (!existingUserName.getId().equals(id))
                                    throw new DataIntegrityViolationException("O nome de usuário já está em uso.");
                            });
            user.setUsername(userRequestDTO.username());
        }

        if (userRequestDTO.email() != null && !userRequestDTO.email().isBlank()) {
            userRepository.findByEmail(userRequestDTO.email())
                            .ifPresent(existingUserEmail -> {
                                if(!existingUserEmail.getId().equals(id))
                                    throw new DataIntegrityViolationException("O email já está em uso.");
                            });
            user.setEmail(userRequestDTO.email());
        }

        User updatedUser = userRepository.save(user);
        return new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail()
        );
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o id: " + id));

        userRepository.delete(user);
    }

}
