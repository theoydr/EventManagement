package com.github.theoydr.eventmanagement.dto;

import com.github.theoydr.eventmanagement.enums.UserRole;

/**
 * Represents the publicly visible data for a user.
 * This DTO excludes sensitive information like the password.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {}
