package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
import com.github.theoydr.eventmanagement.enums.UserRole;
import com.github.theoydr.eventmanagement.exception.OperationNotAllowedException;
import com.github.theoydr.eventmanagement.exception.UserAlreadyExistsException;
import com.github.theoydr.eventmanagement.mapper.UserMapper;
import com.github.theoydr.eventmanagement.model.User;
import com.github.theoydr.eventmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // TODO: Inject a PasswordEncoder here later for security
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User registerUser(UserRegistrationRequest registrationRequest) {
        log.debug("Attempting to register new user with email: {}", registrationRequest.email());

        if (registrationRequest.role() == UserRole.ADMIN) {
            log.warn("Security Alert: User attempted to register as ADMIN with email: {}", registrationRequest.email());

            throw new OperationNotAllowedException("Registration not allowed for role: " + UserRole.ADMIN.name());
        }

        userRepository.findByEmail(registrationRequest.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException(registrationRequest.email());
        });

        User newUser = userMapper.toEntity(registrationRequest);


        User savedUser = userRepository.save(newUser);
        log.info("New user registered successfully with ID: {} and Role: {}", savedUser.getId(), savedUser.getRole());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }
}

