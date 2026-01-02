package com.fitbit.userservice.dto;

import java.time.LocalDateTime;

import com.fitbit.userservice.model.UserRole;

import lombok.Data;

@Data
public class UserResponse {

    private String id;
    private String email;
    private String password;
    private String lastName;
    private String firstName;
    private UserRole role = UserRole.USER;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
