package com.ffaustin.job_tracker.repository;

import com.ffaustin.job_tracker.entity.JobApplication;
import com.ffaustin.job_tracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {
    Optional<JobApplication> findByIdAndUser(Long id, User user);

    Page<JobApplication> findByUser(User user, Pageable pageable);
}
