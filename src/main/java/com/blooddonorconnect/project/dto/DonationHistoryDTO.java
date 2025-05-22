package com.blooddonorconnect.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationHistoryDTO {
    private Long id;
    private Long donorId;

    @NotNull(message = "Donation date is required")
    private LocalDate donationDate;

    private Long requesterId;
    private Long requestId;

    @NotBlank(message = "Location is required")
    private String location;

    private String notes;

    // For display purposes
    private String donorName;
    private String requesterName;
}