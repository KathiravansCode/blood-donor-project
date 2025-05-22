package com.blooddonorconnect.project.controller;

import com.blooddonorconnect.project.dto.AuthResponseDTO;
import com.blooddonorconnect.project.dto.UserDTO;
import com.blooddonorconnect.project.dto.UserLoginDTO;
import com.blooddonorconnect.project.dto.UserRegistrationDTO;
import com.blooddonorconnect.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            AuthResponseDTO response = authService.registerUser(registrationDTO);
            return ResponseEntity.ok(createSuccessResponse("User registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Registration failed", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            AuthResponseDTO response = authService.loginUser(loginDTO);
            return ResponseEntity.ok(createSuccessResponse("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Login failed", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid token", "Token validation failed"));
            }

            String contactNumber = authentication.getName();
            UserDTO user = authService.getCurrentUser(contactNumber);

            return ResponseEntity.ok(createSuccessResponse("Token is valid", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token validation failed", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Unauthorized", "User not authenticated"));
            }

            String contactNumber = authentication.getName();
            UserDTO user = authService.getCurrentUser(contactNumber);

            return ResponseEntity.ok(createSuccessResponse("User retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User not found", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Since we're using stateless JWT, logout is handled on the client side
        // by removing the token from storage
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    // Helper methods for consistent response format
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("error", error);
        return response;
    }
}
