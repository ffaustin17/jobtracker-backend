package com.ffaustin.job_tracker.dto;

import java.time.LocalDate;

public record JobRequest(
        String position,
        String company,
        String jobBoardUsed,
        String applicationLink,
        JobStatus status,
        LocalDate interviewDate,
        LocalDate applicationDate

) { }
