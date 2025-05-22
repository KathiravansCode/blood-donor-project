package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.RequestDonorMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDTO {
    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Response status is required")
    private RequestDonorMapping.Status status;
}