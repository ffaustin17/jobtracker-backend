package com.ffaustin.job_tracker.services;

import com.ffaustin.job_tracker.dto.JobFilterRequest;
import com.ffaustin.job_tracker.dto.JobRequest;
import com.ffaustin.job_tracker.dto.JobStatus;
import com.ffaustin.job_tracker.entity.JobApplication;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.repository.JobApplicationRepository;
import com.ffaustin.job_tracker.repository.UserRepository;
import com.ffaustin.job_tracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobApplicationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JobApplicationRepository jobApplicationRepository;

    @InjectMocks private JobApplicationService jobApplicationService;

    private final String email = "fab@example.com";

    private final User user = User.builder()
            .email(email)
            .firstName("Fabrice")
            .lastName("Faustin")
            .build();

    private final JobRequest jobRequest = new JobRequest(
            "Software Engineer", "Google", "Linkedin",
            "https://example.com/apply", JobStatus.PENDING,
            LocalDate.of(2024, 10, 1), LocalDate.of(2024, 9, 1)
    );

    @Test
    void createJob_shouldSaveJobForAuthenticatedUser(){
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        jobApplicationService.createJob(jobRequest, email);

        verify(userRepository).findByEmail(email);
        verify(jobApplicationRepository).save(any(JobApplication.class));
    }

    @Test
    void getUserJobs_shouldReturnUserJobsPage(){
        Page<JobApplication> mockPage = new PageImpl<>(List.of(new JobApplication()));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobApplicationRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(mockPage);

        Page<JobApplication> result = jobApplicationService.getUserJobs(email, 0, 5);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByEmail(email);
        verify(jobApplicationRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    void updateJob_shouldUpdateJobIfOwnedByUser(){
        JobApplication job = new JobApplication();
        job.setId(1L);
        job.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobApplicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(job));

        jobApplicationService.updateJob(1L, jobRequest, email);

        verify(jobApplicationRepository).save(job);
    }

    @Test
    void deleteJob_shouldDeleteJobIfOwnedByUser(){
        JobApplication job = new JobApplication();

        job.setId(1L);
        job.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobApplicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(job));

        jobApplicationService.deleteJob(1L, jobRequest, email);

        verify(jobApplicationRepository).delete(job);
    }

    @Test
    void filterJobs_shouldApplySpecificationAndReturnPage(){
        JobFilterRequest filter = new JobFilterRequest("PENDING", "walmart", "asc", null);
        Page<JobApplication> mockPage = new PageImpl<>(List.of(new JobApplication()));

        when(jobApplicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        Page<JobApplication> result = jobApplicationService.filterJobs(email, filter, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(jobApplicationRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
