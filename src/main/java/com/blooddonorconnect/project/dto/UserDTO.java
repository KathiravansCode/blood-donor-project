package com.blooddonorconnect.project.dto;

import com.blooddonorconnect.project.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String contactNumber;
    private String email;
    private User.UserType userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}