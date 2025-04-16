package com.ffaustin.job_tracker.service;

import com.ffaustin.job_tracker.dto.JobFilterRequest;
import com.ffaustin.job_tracker.dto.JobRequest;
import com.ffaustin.job_tracker.entity.JobApplication;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.repository.JobApplicationRepository;
import com.ffaustin.job_tracker.repository.UserRepository;
import com.ffaustin.job_tracker.specifications.JobApplicationSpecification;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class JobApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(JobApplicationService.class);

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;


    public JobApplicationService(UserRepository userRepository, JobApplicationRepository jobApplicationRepository){
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }


    /**
     * Creates and saves a new job application associated with the authenticated user
     * @param jobRequest the incoming job application details
     * @param userEmail the email of the authenticated user
     */
    public void createJob(JobRequest jobRequest, String userEmail)
    {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->{
                    logger.warn("Could not find user associated with a job request: {}", userEmail);
                    return new UsernameNotFoundException("User not found: " + userEmail);
                });

        JobApplication job = JobApplication.builder()
                .company(jobRequest.company())
                .position(jobRequest.position())
                .jobBoardUsed(jobRequest.jobBoardUsed())
                .applicationLink(jobRequest.applicationLink())
                .status(jobRequest.status())
                .interviewDate(jobRequest.interviewDate())
                .applicationDate(jobRequest.applicationDate())
                .user(user)
                .build();

        jobApplicationRepository.save(job);

        logger.info("Job application created for user {}: {} at {}", userEmail, jobRequest.position(), jobRequest.company());
    }


    public Page<JobApplication> getUserJobs(String userEmail, int page, int size)
    {
        //retrieve corresponding user object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UsernameNotFoundException("User not found: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);

        return jobApplicationRepository.findByUser(user, pageable);
    }


    public void updateJob(Long jobId, JobRequest request, String userEmail) {
        //find the user
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->{
            logger.warn("User not found: {}", userEmail);
            return new UsernameNotFoundException("User not found");
        });

        //retrieve the specific job
        JobApplication job = jobApplicationRepository.findByIdAndUser(jobId, user).orElseThrow(
                ()->new NoSuchElementException("Job not found for this user.")
        );

        //update the job
        job.setCompany(request.company());
        job.setPosition(request.position());
        job.setJobBoardUsed(request.jobBoardUsed());
        job.setApplicationLink(request.applicationLink());
        job.setStatus(request.status());
        job.setDateApplied(request.applicationDate());
        job.setInterviewDate(request.interviewDate());

        jobApplicationRepository.save(job);

        logger.info("Job at id {} updated successfully.", jobId);
    }


    public void deleteJob(Long jobId, @Valid JobRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UsernameNotFoundException("User not found: " + userEmail));

        JobApplication job = jobApplicationRepository.findByIdAndUser(jobId, user)
                .orElseThrow(()-> new NoSuchElementException("The job specified could not be found for this user"));

        jobApplicationRepository.delete(job);

        logger.info("Deleted job {} for user {}", jobId, userEmail);
    }


    public Page<JobApplication> filterJobs(String userEmail, JobFilterRequest filter, int page, int size){
        Specification<JobApplication> specs = JobApplicationSpecification.withFilters(userEmail, filter);

        Sort sort = Sort.by("appliedDate").descending(); //default sorting

        if(filter.sortBy() != null && filter.direction() != null){
            Sort.Direction direction = Sort.Direction.fromString(filter.direction());
            sort = Sort.by(direction, filter.sortBy());
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return jobApplicationRepository.findAll(specs, pageable);
    }
}
