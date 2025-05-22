package com.blooddonorconnect.project.controller;

import com.blooddonorconnect.project.dto.*;
import com.blooddonorconnect.project.model.DonorProfile;
import com.blooddonorconnect.project.model.User;
import com.blooddonorconnect.project.repository.UserRepository;
import com.blooddonorconnect.project.service.DonationHistoryService;
import com.blooddonorconnect.project.service.DonationRequestService;
import com.blooddonorconnect.project.service.DonorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donors")
@CrossOrigin(origins = "http://localhost:3000")
public class DonorController {

    @Autowired
    private DonorProfileService donorProfileService;

    @Autowired
    private DonationHistoryService donationHistoryService;

    @Autowired
    private DonationRequestService donationRequestService;

    @Autowired
    private UserRepository userRepository;

    // Profile Management
    @PostMapping("/profile")
    public ResponseEntity<?> createDonorProfile(@Valid @RequestBody DonorProfileDTO donorProfileDTO,
                                                Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            DonorProfileDTO createdProfile = donorProfileService.createDonorProfile(userId, donorProfileDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Donor profile created successfully", createdProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to create donor profile", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getDonorProfile(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            DonorProfileDTO profile = donorProfileService.getDonorProfile(userId);
            return ResponseEntity.ok(createSuccessResponse("Donor profile retrieved successfully", profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Donor profile not found", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateDonorProfile(@Valid @RequestBody DonorProfileDTO donorProfileDTO,
                                                Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            DonorProfileDTO updatedProfile = donorProfileService.updateDonorProfile(userId, donorProfileDTO);
            return ResponseEntity.ok(createSuccessResponse("Donor profile updated successfully", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to update donor profile", e.getMessage()));
        }
    }

    // Availability Management
    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, Object> availabilityData,
                                                Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);

            DonorProfile.AvailabilityStatus status = DonorProfile.AvailabilityStatus
                    .valueOf((String) availabilityData.get("status"));
            String reason = (String) availabilityData.get("reason");
            LocalDate unavailableUntil = availabilityData.get("unavailableUntil") != null ?
                    LocalDate.parse((String) availabilityData.get("unavailableUntil")) : null;

            DonorProfileDTO updatedProfile = donorProfileService.updateAvailabilityStatus(userId, status, reason, unavailableUntil);
            return ResponseEntity.ok(createSuccessResponse("Availability status updated successfully", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to update availability", e.getMessage()));
        }
    }

    // Donation Requests
    @GetMapping("/requests")
    public ResponseEntity<?> getDonorRequests(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<DonationRequestDTO> requests = donationRequestService.getDonorRequests(userId);
            return ResponseEntity.ok(createSuccessResponse("Donation requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve requests", e.getMessage()));
        }
    }

    @PutMapping("/requests/{requestId}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long requestId,
                                              @Valid @RequestBody RequestResponseDTO responseDTO,
                                              Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            responseDTO.setRequestId(requestId);
            donationRequestService.respondToRequest(userId, responseDTO);
            return ResponseEntity.ok(createSuccessResponse("Response submitted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to respond to request", e.getMessage()));
        }
    }

    // Donation History
    @GetMapping("/history")
    public ResponseEntity<?> getDonationHistory(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<DonationHistoryDTO> history = donationHistoryService.getDonorHistory(userId);
            return ResponseEntity.ok(createSuccessResponse("Donation history retrieved successfully", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve donation history", e.getMessage()));
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> logDonation(@Valid @RequestBody DonationHistoryDTO donationHistoryDTO,
                                         Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            donationHistoryDTO.setDonorId(userId);
            DonationHistoryDTO loggedDonation = donationHistoryService.logDonation(donationHistoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("Donation logged successfully", loggedDonation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to log donation", e.getMessage()));
        }
    }

    @GetMapping("/history/last")
    public ResponseEntity<?> getLastDonation(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            DonationHistoryDTO lastDonation = donationHistoryService.getLastDonation(userId);
            return ResponseEntity.ok(createSuccessResponse("Last donation retrieved successfully", lastDonation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve last donation", e.getMessage()));
        }
    }

    @GetMapping("/eligibility")
    public ResponseEntity<?> checkDonationEligibility(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            boolean canDonate = donationHistoryService.canDonateToday(userId);
            LocalDate nextEligibleDate = donationHistoryService.getNextEligibleDate(userId);
            int totalDonations = donationHistoryService.getTotalDonationsCount(userId);

            Map<String, Object> eligibilityData = new HashMap<>();
            eligibilityData.put("canDonate", canDonate);
            eligibilityData.put("nextEligibleDate", nextEligibleDate);
            eligibilityData.put("totalDonations", totalDonations);

            return ResponseEntity.ok(createSuccessResponse("Eligibility checked successfully", eligibilityData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to check eligibility", e.getMessage()));
        }
    }

    // Helper methods
    private Long getCurrentUserId(Authentication authentication) {
        String contactNumber = authentication.getName();
        User user = userRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
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
