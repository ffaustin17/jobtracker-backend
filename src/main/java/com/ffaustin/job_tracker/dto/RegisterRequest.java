package com.ffaustin.job_tracker.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotNull
        @NotBlank(message = "First name is required")
        String firstName,

        @NotNull
        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotNull
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
        @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")
        @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one number")
        String password
){}
