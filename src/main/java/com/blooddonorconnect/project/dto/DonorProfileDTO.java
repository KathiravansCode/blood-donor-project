package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.DonorProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileDTO {
    private Long donorId;

    @NotNull(message = "Blood group is required")
    private DonorProfile.BloodGroup bloodGroup;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "Availability status is required")
    private DonorProfile.AvailabilityStatus availabilityStatus;

    private String statusReason;
    private LocalDate unavailableUntil;
    private LocalDate nextEligibleDonationDate;

    // For display purposes
    private String donorName;
    private String donorContactNumber;
}