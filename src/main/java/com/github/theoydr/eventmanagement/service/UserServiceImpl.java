package com.github.theoydr.eventmanagement.service;

import com.github.theoydr.eventmanagement.dto.UserRegistrationRequest;
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

        userRepository.findByEmail(registrationRequest.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException(registrationRequest.email());
        });

        User newUser = userMapper.toEntity(registrationRequest);

        // --- SECURITY NOTE ---
        // In a real application, we would hash the password here before saving.
        // For example: newUser.setPassword(passwordEncoder.encode(registrationRequest.password()));
        User savedUser = userRepository.save(newUser);
        log.info("New user registered successfully with ID: {}", savedUser.getId());
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

