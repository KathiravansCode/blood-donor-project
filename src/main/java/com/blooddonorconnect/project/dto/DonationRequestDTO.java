package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.DonationRequest;
import com.blooddonorconnect.project.model.DonorProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequestDTO {
    private Long id;
    private Long requesterId;

    @NotNull(message = "Blood group needed is required")
    private DonorProfile.BloodGroup bloodGroupNeeded;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    @NotNull(message = "Urgency is required")
    private DonationRequest.Urgency urgency;

    @NotBlank(message = "Message is required")
    private String message;

    private DonationRequest.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For display purposes
    private String requesterName;
    private String requesterContactNumber;
}