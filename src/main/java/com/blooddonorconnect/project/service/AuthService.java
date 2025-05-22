package com.blooddonorconnect.project.service;

import com.blooddonorconnect.project.dto.AuthResponseDTO;
import com.blooddonorconnect.project.dto.UserDTO;
import com.blooddonorconnect.project.dto.UserLoginDTO;
import com.blooddonorconnect.project.dto.UserRegistrationDTO;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.UserRepository;
import com.blooddonorconnect.project.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        // Check if user already exists
        if (userRepository.existsByContactNumber(registrationDTO.getContactNumber())) {
            throw new RuntimeException("User with this contact number already exists");
        }

        if (registrationDTO.getEmail() != null && userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setName(registrationDTO.getName());
        user.setContactNumber(registrationDTO.getContactNumber());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setUserType(registrationDTO.getUserType());

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getContactNumber());
        String token = jwtUtil.generateToken(userDetails);

        // Create userDTO
        UserDTO userDTO = convertToUserDTO(savedUser);

        return new AuthResponseDTO(token, userDTO);
    }

    public AuthResponseDTO loginUser(UserLoginDTO loginDTO) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getContactNumber(),
                            loginDTO.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getContactNumber());

            // Get user from database
            User user = userRepository.findByContactNumber(loginDTO.getContactNumber())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Create user DTO
            UserDTO userDTO = convertToUserDTO(user);

            return new AuthResponseDTO(token, userDTO);

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid contact number or password");
        }
    }

    public UserDTO getCurrentUser(String contactNumber) {
        User user = userRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToUserDTO(user);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setContactNumber(user.getContactNumber());
        userDTO.setEmail(user.getEmail());
        userDTO.setUserType(user.getUserType());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }
}