package com.github.theoydr.eventmanagement.controller;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.dto.UserResponse;
import com.github.theoydr.eventmanagement.exception.ResourceNotFoundException;
import com.github.theoydr.eventmanagement.mapper.UserMapper;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        User registeredUser = userService.registerUser(registrationRequest);
        UserResponse userResponse = userMapper.toResponse(registeredUser);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAllUsers().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", id));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
