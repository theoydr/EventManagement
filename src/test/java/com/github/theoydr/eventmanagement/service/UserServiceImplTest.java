package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.enums.UserRole;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.UserAlreadyExistsException;
import com.github.theoydr.eventmanagement.mapper.UserMapper;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should successfully register a user when role is USER and email is unique")
    void registerUser_Success() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "john@example.com",
                "password123",
                UserRole.USER
        );

        User mappedUser = new User();
        mappedUser.setEmail(request.email());
        mappedUser.setRole(UserRole.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.email());
        savedUser.setRole(UserRole.USER);

        // Define Mock Behavior
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty()); // Email does not exist
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        // Verify interactions
        verify(userRepository).save(mappedUser);
    }

    @Test
    @DisplayName("Should throw exception when registering as ADMIN via public API")
    void registerUser_AdminRole_ThrowsException() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Hacker",
                "hacker@example.com",
                "password123",
                UserRole.ADMIN
        );

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(OperationNotAllowedException.class)
                .hasMessageContaining("Registration not allowed for role: ADMIN");

        // Verify that we NEVER touched the database
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_DuplicateEmail_ThrowsException() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Jane Doe",
                "jane@example.com",
                "password123",
                UserRole.USER
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        // Verify we checked for the user but NEVER saved
        verify(userRepository).findByEmail(request.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return empty optional when user not found by ID")
    void findUserById_NotFound_ReturnsEmpty() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findUserById(userId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return user when found by ID")
    void findUserById_Found_ReturnsUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findUserById(userId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should return all users")
    void findAllUsers_ReturnsList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertThat(result).hasSize(2);
    }
}