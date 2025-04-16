package com.ffaustin.job_tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private boolean enabled = false;

    private boolean accountLocked = false;

    private int failedLoginAttempts  = 0;

    private LocalDateTime lockTime;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    //builder
    public static UserBuilder builder(){
        return new UserBuilder();
    }

    public static class UserBuilder{
        private final User user;

        public UserBuilder(){
            user = new User();
        }

        public UserBuilder email(String email){
            user.email = email;
            return this;
        }

        public UserBuilder firstName(String firstName){
            user.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName){
            user.lastName = lastName;
            return this;
        }

        public UserBuilder password(String password){
            user.password = password;
            return this;
        }

        public UserBuilder enabled(boolean enabled) {
            user.enabled = enabled;
            return this;
        }

        public UserBuilder accountLocked(boolean accountLocked) {
            user.accountLocked = accountLocked;
            return this;
        }

        public UserBuilder failedLoginAttempts(int attempts) {
            user.failedLoginAttempts = attempts;
            return this;
        }

        public User build() {
            return user;
        }

    }
}
