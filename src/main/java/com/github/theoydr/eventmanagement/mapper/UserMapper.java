package com.github.theoydr.eventmanagement.mapper;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.dto.UserResponse;
import com.github.theoydr.eventmanagement.model.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {


    public User toEntity(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password()); // Password will be hashed by the service
        return user;
    }


    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
