package com.ffaustin.job_tracker.controller;

import com.ffaustin.job_tracker.dto.ApiResponse;
import com.ffaustin.job_tracker.dto.JobFilterRequest;
import com.ffaustin.job_tracker.dto.JobRequest;
import com.ffaustin.job_tracker.entity.JobApplication;
import com.ffaustin.job_tracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/jobs")
public class JobApplicationController {
    private static final Logger logger = LoggerFactory.getLogger(JobApplicationController.class);

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService){
        this.jobApplicationService = jobApplicationService;
    }


    /**
     * Retrieves a paginated list of job applications for the authenticated user.
     * @param principal the current user (identified by email)
     * @param page the page number (default 0)
     * @param size the size per page (default 10)
     * @return paginated job applications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobApplication>>> getUserJobs(
        Principal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    )
    {
        Page<JobApplication> jobs = jobApplicationService.getUserJobs(principal.getName(), page, size);

        return ResponseEntity.ok(
                ApiResponse.of(200, "Job applications fetched successfully", jobs)
        );
    }


    /**
     * Create a new job application for an authenticated user.
     * @param jobRequest
     * @param principal
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createAJobApplication(
            @RequestBody @Valid JobRequest jobRequest,
            Principal principal)
    {
        jobApplicationService.createJob(jobRequest, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(201, "Job created successfully", null));
    }


    /**
     * Update an existing job application for an authenticated user.
     * @param jobId the id specifying the job
     * @param request
     * @param principal
     * @return
     */
    @PutMapping("/{jobId}")
    public ResponseEntity<ApiResponse<String>> updateJobApplication(
            @PathVariable Long jobId,
            @RequestBody @Valid JobRequest request,
            Principal principal
    )
    {
        jobApplicationService.updateJob(jobId, request, principal.getName());

        return ResponseEntity.ok(
                ApiResponse.of(200, "Job updated successfully", null)
        );
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<String>> deleteJobApplication(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            Principal principal)
    {
        jobApplicationService.deleteJob(jobId, request, principal.getName());

        return ResponseEntity.ok(ApiResponse.of(200, "Job deleted successfully", null));

    }


    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<JobApplication>>> filterJobs(
            @RequestBody JobFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    )
    {
        Page<JobApplication> result = jobApplicationService.filterJobs(principal.getName(), filterRequest, page, size);

        return ResponseEntity.ok(ApiResponse.of(200, "Jobs filtered successfulyy", result));
    }

}
