package com.github.theoydr.eventmanagement.dto;


import com.github.theoydr.eventmanagement.constants.MessageKeys;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Represents the data required to register a new user.
 */
public record UserRegistrationRequest(
        @NotBlank(message = MessageKeys.UserMessages.USERNAME_NOT_BLANK)
        @Size(min = 3, max = 20, message = MessageKeys.UserMessages.USERNAME_SIZE)
        String username,

        @NotBlank(message = MessageKeys.UserMessages.EMAIL_NOT_BLANK)
        @Email(message = MessageKeys.UserMessages.EMAIL_FORMAT)
        String email,

        @NotBlank(message = MessageKeys.UserMessages.PASSWORD_NOT_BLANK)
        @Size(min = 8, max=16, message = MessageKeys.UserMessages.PASSWORD_SIZE)
        String password
) {}
