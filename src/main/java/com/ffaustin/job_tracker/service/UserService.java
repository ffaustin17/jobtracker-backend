package com.ffaustin.job_tracker.service;

import com.ffaustin.job_tracker.dto.UserResponse;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Fetches details of the current authenticated user.
     * @return a UserResponse DTO
     * @throws AccessDeniedException if no valid user is in context
     */
    public UserResponse getCurrentUser() throws AccessDeniedException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserDetails userDetails){
            String email = userDetails.getUsername();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(()->{
                        logger.warn("User not found in database: {}", email);
                        return new UsernameNotFoundException("User not found");
                    });

            logger.info("Authenticated user accessed profile: {}", email);

            return new UserResponse(
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
        }

        logger.warn("Unauthorized access attempt detected.");
        throw new AccessDeniedException("Unauthorized");
    }


    /**
     * Allows the authenticated user to update their password.
     * @param currentPassword the user's current password
     * @param newPassword the new password they wish to set.
     */
    public void updatePassword(String currentPassword, String newPassword){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserDetails userDetails){
            String email = userDetails.getUsername();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(()->{
                        logger.warn("User not found during password update: {}", email);
                        return new UsernameNotFoundException("User not found");
                    });

            if(!passwordEncoder.matches(currentPassword, user.getPassword())){
                logger.warn("User provided a current password that does not match their actual password: {}", email);
                throw new IllegalArgumentException("Current password provided is incorrect.");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password updated successfully for use: {}", email);
        }
    }


}
