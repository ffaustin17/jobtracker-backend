package com.ffaustin.job_tracker.dto;

public record UserResponse(
        String email,
        String firstName,
        String lastName
) { }
