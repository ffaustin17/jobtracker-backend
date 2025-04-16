package com.ffaustin.job_tracker.entity;

import com.ffaustin.job_tracker.dto.JobStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String position;
    private String company;
    private String jobBoardUsed;
    private String applicationLink;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDate dateApplied;
    private LocalDate interviewDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public JobApplication() {
    }

    public JobApplication(
            String position,
            String company,
            String jobBoardUsed,
            String applicationLink,
            JobStatus status,
            LocalDate dateApplied,
            LocalDate interviewDate, User user)
    {
        this.position = position;
        this.company = company;
        this.jobBoardUsed = jobBoardUsed;
        this.applicationLink = applicationLink;
        this.status = status;
        this.dateApplied = dateApplied;
        this.interviewDate = interviewDate;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobBoardUsed() {
        return jobBoardUsed;
    }

    public void setJobBoardUsed(String jobBoardUsed) {
        this.jobBoardUsed = jobBoardUsed;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public void setDateApplied(LocalDate dateApplied) {
        this.dateApplied = dateApplied;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final JobApplication job = new JobApplication();

        public Builder position(String position){
            job.position = position;
            return this;
        }

        public Builder company(String company){
            job.company = company;
            return this;
        }

        public Builder jobBoardUsed(String jobBoard){
            job.jobBoardUsed = jobBoard;
            return this;
        }

        public Builder applicationLink(String applicationLink){
            job.applicationLink = applicationLink;
            return this;
        }

        public Builder status(JobStatus status){
            job.status = status;
            return this;
        }

        public Builder interviewDate(LocalDate interviewDate){
            job.interviewDate = interviewDate;
            return this;
        }

        public Builder applicationDate(LocalDate dateApplied){
            job.dateApplied = dateApplied;
            return this;
        }

        public Builder user(User user){
            job.user = user;
            return this;
        }


        public JobApplication build(){
            return job;
        }
    }
}
