package com.blooddonorconnect.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
