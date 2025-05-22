package com.blooddonorconnect.project.controller;

import com.blooddonorconnect.project.dto.DonationRequestDTO;
import com.blooddonorconnect.project.dto.UserDTO;
import com.blooddonorconnect.project.model.DonationRequest;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.UserRepository;
import com.blooddonorconnect.project.service.AuthService;
import com.blooddonorconnect.project.service.DonationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requesters")
@CrossOrigin(origins = "http://localhost:3000")
public class RequesterController {

    @Autowired
    private DonationRequestService donationRequestService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getRequesterProfile(Authentication authentication) {
        try {
            String contactNumber = authentication.getName();
            UserDTO user = authService.getCurrentUser(contactNumber);
            return ResponseEntity.ok(createSuccessResponse("Requester profile retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Requester profile not found", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateRequesterProfile(@RequestBody Map<String, String> updates,
                                                    Authentication authentication) {
        try {
            // Note: This is a simplified update. In a full implementation,
            // you might want to create a dedicated service method for profile updates
            Long userId = getCurrentUserId(authentication);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update allowed fields
            if (updates.containsKey("name")) {
                user.setName(updates.get("name"));
            }
            if (updates.containsKey("email")) {
                user.setEmail(updates.get("email"));
            }

            User updatedUser = userRepository.save(user);
            UserDTO userDTO = convertToUserDTO(updatedUser);

            return ResponseEntity.ok(createSuccessResponse("Profile updated successfully", userDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to update profile", e.getMessage()));
        }
    }

    // Donation Request Management
    @PostMapping("/requests")
    public ResponseEntity<?> createDonationRequest(@Valid @RequestBody DonationRequestDTO requestDTO,
                                                   Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            DonationRequestDTO createdRequest = donationRequestService.createDonationRequest(userId, requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Donation request created successfully", createdRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to create donation request", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getRequesterRequests(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<DonationRequestDTO> requests = donationRequestService.getRequesterRequests(userId);
            return ResponseEntity.ok(createSuccessResponse("Requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve requests", e.getMessage()));
        }
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<?> getDonationRequest(@PathVariable Long requestId, Authentication authentication) {
        try {
            // Verify the request belongs to the current user
            Long userId = getCurrentUserId(authentication);
            DonationRequestDTO request = donationRequestService.getDonationRequest(requestId);

            if (!request.getRequesterId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Access denied", "Request does not belong to current user"));
            }

            return ResponseEntity.ok(createSuccessResponse("Request retrieved successfully", request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Request not found", e.getMessage()));
        }
    }

    @PutMapping("/requests/{requestId}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long requestId,
                                                 @RequestBody Map<String, String> statusUpdate,
                                                 Authentication authentication) {
        try {
            // Verify the request belongs to the current user
            Long userId = getCurrentUserId(authentication);
            DonationRequestDTO request = donationRequestService.getDonationRequest(requestId);

            if (!request.getRequesterId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Access denied", "Request does not belong to current user"));
            }

            DonationRequest.Status status = DonationRequest.Status.valueOf(statusUpdate.get("status"));
            DonationRequestDTO updatedRequest = donationRequestService.updateRequestStatus(requestId, status);

            return ResponseEntity.ok(createSuccessResponse("Request status updated successfully", updatedRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to update request status", e.getMessage()));
        }
    }

    @PutMapping("/requests/{requestId}/fulfill")
    public ResponseEntity<?> fulfillRequest(@PathVariable Long requestId,
                                            @RequestBody Map<String, Long> fulfillmentData,
                                            Authentication authentication) {
        try {
            // Verify the request belongs to the current user
            Long userId = getCurrentUserId(authentication);
            DonationRequestDTO request = donationRequestService.getDonationRequest(requestId);

            if (!request.getRequesterId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Access denied", "Request does not belong to current user"));
            }

            Long donorId = fulfillmentData.get("donorId");
            donationRequestService.fulfillRequest(requestId, donorId);

            return ResponseEntity.ok(createSuccessResponse("Request fulfilled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to fulfill request", e.getMessage()));
        }
    }

    @GetMapping("/requests/active")
    public ResponseEntity<?> getActiveRequests(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<DonationRequestDTO> requests = donationRequestService.getRequesterRequests(userId);

            // Filter active requests
            List<DonationRequestDTO> activeRequests = requests.stream()
                    .filter(req -> req.getStatus() == DonationRequest.Status.ACTIVE)
                    .toList();

            return ResponseEntity.ok(createSuccessResponse("Active requests retrieved successfully", activeRequests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve active requests", e.getMessage()));
        }
    }

    @GetMapping("/requests/history")
    public ResponseEntity<?> getRequestHistory(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<DonationRequestDTO> requests = donationRequestService.getRequesterRequests(userId);

            // Filter completed requests
            List<DonationRequestDTO> completedRequests = requests.stream()
                    .filter(req -> req.getStatus() == DonationRequest.Status.FULFILLED ||
                            req.getStatus() == DonationRequest.Status.EXPIRED)
                    .toList();

            return ResponseEntity.ok(createSuccessResponse("Request history retrieved successfully", completedRequests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve request history", e.getMessage()));
        }
    }

    // Helper methods
    private Long getCurrentUserId(Authentication authentication) {
        String contactNumber = authentication.getName();
        User user = userRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
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
