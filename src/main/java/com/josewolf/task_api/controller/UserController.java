package com.josewolf.task_api.controller;

import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.dto.responsedto.UserResponseDTO;
import com.josewolf.task_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuário", description = "Responsável por ações do usuário")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Criação do usuário")
    @ApiResponse(responseCode = "201", description = "Username e email validados com sucesso.")
    @ApiResponse(responseCode = "400", description = "O corpo da requisição está ausente ou possui campos inválidos.")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.createUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }


    @GetMapping
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários criados")
    @ApiResponse(responseCode = "200", description = "Todos os usuários existentes são listados.")
    @ApiResponse(responseCode = "404", description = "Não existem usuários para serem listados.")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userResponseDTOS = userService.findAllUsers();
        return ResponseEntity.ok(userResponseDTOS);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca o usuário pelo username", description = "Retorna os dados do usuários pelo username.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado pelo username solicitado.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o username solicitado.")
    public ResponseEntity<UserResponseDTO> getByUserName(@RequestParam String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/search/email")
    @Operation(summary = "Busca o usuário pelo email", description = "Retorna os dados do usuários pelo email.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado pelo email solicitado.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o email solicitado.")
    public ResponseEntity<UserResponseDTO> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza o username ou email por id", description = "Atualiza o username ou email.")
    @ApiResponse(responseCode = "200", description = "Email ou username foram alterados.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o Id solicitado.")
    @ApiResponse(responseCode = "409", description = "Email ou username já em uso por outro usuário.")
    public ResponseEntity<UserResponseDTO> patch(@PathVariable Long id, @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO response = userService.updateUserById(id, userRequestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui o usuário", description = "Exclui o usuário por id.")
    @ApiResponse(responseCode = "204", description = "Usuário deletado.")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado com o Id solicitado.")
    public ResponseEntity<Void>  deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
