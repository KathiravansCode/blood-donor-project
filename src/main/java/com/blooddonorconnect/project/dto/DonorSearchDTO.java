package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.DonorProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorSearchDTO {
    @NotNull(message = "Blood group is required for search")
    private DonorProfile.BloodGroup bloodGroup;

    private String location; // Can be city or pincode
}
