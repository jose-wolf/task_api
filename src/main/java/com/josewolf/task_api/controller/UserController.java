package com.josewolf.task_api.controller;

import com.josewolf.task_api.dto.requestdto.UserRequestDTO;
import com.josewolf.task_api.dto.responsedto.UserResponseDTO;
import com.josewolf.task_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.createUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userResponseDTOS = userService.findAllUsers();
        return ResponseEntity.ok(userResponseDTOS);
    }

    @GetMapping("/search")
    public ResponseEntity<UserResponseDTO> getByUserName(@RequestParam String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/search/email")
    public ResponseEntity<UserResponseDTO> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> patch(@PathVariable Long id, @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO response = userService.updateUserById(id, userRequestDTO);
        return ResponseEntity.ok(response);
    }
}
