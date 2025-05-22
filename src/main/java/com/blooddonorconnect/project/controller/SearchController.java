package com.blooddonorconnect.project.controller;

import com.blooddonorconnect.project.dto.DonorProfileDTO;
import com.blooddonorconnect.project.dto.DonorSearchDTO;
import com.blooddonorconnect.project.model.DonorProfile;
import com.blooddonorconnect.project.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/donors")
    public ResponseEntity<?> searchDonors(@Valid @RequestBody DonorSearchDTO searchDTO) {
        try {
            List<DonorProfileDTO> donors = searchService.searchDonors(searchDTO);
            return ResponseEntity.ok(createSuccessResponse("Donors found successfully", donors));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/donors")
    public ResponseEntity<?> searchDonorsWithParams(@RequestParam("bloodGroup") String bloodGroup,
                                                    @RequestParam(value = "location", required = false) String location) {
        try {
            DonorProfile.BloodGroup bloodGroupEnum = DonorProfile.BloodGroup.valueOf(bloodGroup.toUpperCase());
            DonorSearchDTO searchDTO = new DonorSearchDTO(bloodGroupEnum, location);

            List<DonorProfileDTO> donors = searchService.searchDonors(searchDTO);
            return ResponseEntity.ok(createSuccessResponse("Donors found successfully", donors));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid blood group", "Please provide a valid blood group"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/donors/compatible")
    public ResponseEntity<?> searchCompatibleDonors(@RequestParam("bloodGroup") String bloodGroup,
                                                    @RequestParam(value = "location", required = false) String location) {
        try {
            DonorProfile.BloodGroup bloodGroupEnum = DonorProfile.BloodGroup.valueOf(bloodGroup.toUpperCase());

            List<DonorProfileDTO> donors = searchService.searchCompatibleDonors(bloodGroupEnum, location);
            return ResponseEntity.ok(createSuccessResponse("Compatible donors found successfully", donors));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid blood group", "Please provide a valid blood group"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/donors/city/{city}")
    public ResponseEntity<?> searchDonorsByCity(@PathVariable String city,
                                                @RequestParam("bloodGroup") String bloodGroup) {
        try {
            DonorProfile.BloodGroup bloodGroupEnum = DonorProfile.BloodGroup.valueOf(bloodGroup.toUpperCase());

            List<DonorProfileDTO> donors = searchService.searchDonorsByCity(bloodGroupEnum, city);
            return ResponseEntity.ok(createSuccessResponse("Donors found in city successfully", donors));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid blood group", "Please provide a valid blood group"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/donors/pincode/{pincode}")
    public ResponseEntity<?> searchDonorsByPincode(@PathVariable String pincode,
                                                   @RequestParam("bloodGroup") String bloodGroup) {
        try {
            DonorProfile.BloodGroup bloodGroupEnum = DonorProfile.BloodGroup.valueOf(bloodGroup.toUpperCase());

            List<DonorProfileDTO> donors = searchService.searchDonorsByPincode(bloodGroupEnum, pincode);
            return ResponseEntity.ok(createSuccessResponse("Donors found in pincode successfully", donors));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid blood group", "Please provide a valid blood group"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Search failed", e.getMessage()));
        }
    }

    @GetMapping("/donors/count")
    public ResponseEntity<?> getAvailableDonorCount(@RequestParam("bloodGroup") String bloodGroup,
                                                    @RequestParam(value = "location", required = false) String location) {
        try {
            DonorProfile.BloodGroup bloodGroupEnum = DonorProfile.BloodGroup.valueOf(bloodGroup.toUpperCase());

            long count = searchService.getAvailableDonorCount(bloodGroupEnum, location);

            Map<String, Object> countData = new HashMap<>();
            countData.put("bloodGroup", bloodGroup);
            countData.put("location", location);
            countData.put("availableDonors", count);

            return ResponseEntity.ok(createSuccessResponse("Donor count retrieved successfully", countData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid blood group", "Please provide a valid blood group"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to get donor count", e.getMessage()));
        }
    }

    @GetMapping("/blood-groups")
    public ResponseEntity<?> getAvailableBloodGroups() {
        try {
            Map<String, String> bloodGroups = new HashMap<>();
            for (DonorProfile.BloodGroup bloodGroup : DonorProfile.BloodGroup.values()) {
                bloodGroups.put(bloodGroup.name(), bloodGroup.getDisplay());
            }

            return ResponseEntity.ok(createSuccessResponse("Blood groups retrieved successfully", bloodGroups));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve blood groups", e.getMessage()));
        }
    }

    @GetMapping("/donors/summary")
    public ResponseEntity<?> getDonorsSummary(@RequestParam(value = "location", required = false) String location) {
        try {
            Map<String, Long> summary = new HashMap<>();

            for (DonorProfile.BloodGroup bloodGroup : DonorProfile.BloodGroup.values()) {
                long count = searchService.getAvailableDonorCount(bloodGroup, location);
                summary.put(bloodGroup.getDisplay(), count);
            }

            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("location", location != null ? location : "All locations");
            summaryData.put("bloodGroupCounts", summary);
            summaryData.put("totalDonors", summary.values().stream().mapToLong(Long::longValue).sum());

            return ResponseEntity.ok(createSuccessResponse("Donors summary retrieved successfully", summaryData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to retrieve donors summary", e.getMessage()));
        }
    }

    // Helper methods
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