package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.dto.UserResponse;
import com.github.theoydr.eventmanagement.enums.UserRole;
import com.github.theoydr.eventmanagement.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    @DisplayName("Should map registration request to entity and default to USER role if null")
    void toEntity_DefaultRole() {
        // Arrange
        // Passing null for the role to test the default logic
        UserRegistrationRequest request = new UserRegistrationRequest(
                "John Doe",
                "john@example.com",
                "password123",
                null
        );

        // Act
        User user = userMapper.toEntity(request);

        // Assert
        assertThat(user.getUsername()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");

        // Critical check: Ensure logic defaults to USER when input is null
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should map registration request to entity with specific role")
    void toEntity_SpecificRole() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest(
                "Organizer Jane",
                "jane@example.com",
                "password123",
                UserRole.ORGANIZER
        );

        // Act
        User user = userMapper.toEntity(request);

        // Assert
        assertThat(user.getUsername()).isEqualTo("Organizer Jane");
        assertThat(user.getRole()).isEqualTo(UserRole.ORGANIZER);
    }

    @Test
    @DisplayName("Should map entity to response DTO")
    void toResponse() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("John Doe");
        user.setEmail("john@example.com");
        user.setRole(UserRole.ADMIN);

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    }
}