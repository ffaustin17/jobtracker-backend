package com.ffaustin.job_tracker.dto;

public record JobFilterRequest(
        String status,
        String company,
        String sortBy,
        String direction
) {
}
