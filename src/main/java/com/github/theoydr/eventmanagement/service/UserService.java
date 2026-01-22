package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.UserAlreadyExistsException;
import com.github.theoydr.eventmanagement.model.User;

import java.util.List;
import java.util.Optional;


/**
 * Service interface for managing users.
 * Defines the contract for user-related business operations.
 */
public interface UserService {

    /**
     * Registers a new user in the system.
     *
     * @param registrationRequest DTO containing the new user's details.
     * @return The newly created and persisted User entity.
     * @throws OperationNotAllowedException if a forbidden role tries to register.
     * @throws UserAlreadyExistsException if a user with the same email already exists.
     */

    User registerUser(UserRegistrationRequest registrationRequest);


    /**
     * Finds a user by their unique ID.
     *
     * @param id The ID of the user to find.
     * @return an {@link Optional} containing the found user, or {@link Optional#empty()} if no user is found.
     */
    Optional<User> findUserById(Long id);


    /**
     * Finds a user by their unique ID.
     *
     * @param email The email of the user to find.
     * @return an {@link Optional} containing the found user, or {@link Optional#empty()} if no user is found.
     */
    Optional<User> findUserByEmail(String email);



    /**
     * Retrieves a list of all users.
     *
     * @return A list of all User entities.
     */
    List<User> findAllUsers();
}
