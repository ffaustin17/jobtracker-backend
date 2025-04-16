package com.ffaustin.job_tracker.service;

import com.ffaustin.job_tracker.dto.LoginRequest;
import com.ffaustin.job_tracker.dto.LoginResponse;
import com.ffaustin.job_tracker.dto.RegisterRequest;
import com.ffaustin.job_tracker.entity.PasswordResetToken;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.entity.VerificationToken;
import com.ffaustin.job_tracker.repository.PasswordResetTokenRepository;
import com.ffaustin.job_tracker.repository.UserRepository;
import com.ffaustin.job_tracker.repository.VerificationTokenRepository;
import com.ffaustin.job_tracker.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthService(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            EmailService emailService,
            BCryptPasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            JwtUtil jwtUtil,
            PasswordResetTokenRepository passwordResetTokenRepository)
    {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }


    /**
     * Handles new user registration. Saves user and sends email verification token.
     * @param registerRequest the user's registration request
     */
    @Transactional
    public void register(RegisterRequest registerRequest){
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.email());

        if(existingUser.isPresent()){
            logger.warn("Attempt to register already existing email: {}", registerRequest.email());
            throw new RuntimeException("Email already registered.");
        }

        User user = new User();
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setEnabled(false); //must verify account before enabling account

        userRepository.save(user);

        //generate token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        verificationTokenRepository.save(verificationToken);

        //send email
        emailService.sendVerificationEmail(user.getEmail(), token);
        logger.info("Verification token sent to user: {}", user.getEmail());
    }


    /**
     * Verifies the email token sent during registration.
     * @param token registration token
     */
    @Transactional
    public void verifyEmailToken(String token){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(()->{
                    logger.warn("Invalid verification token received: {}", token);
                    return new RuntimeException("Invalid verification token.");
                });

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            logger.warn("Expired verification token attempted: {}",token);
            throw new IllegalStateException("Verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        logger.info("User verified successfully: {}", user.getEmail());
    }


    /**
     * Authenticates the user and issues a JWT if credentials are valid.
     * @param request
     * @return
     */
    public LoginResponse login(LoginRequest request){

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                ));

        User user = userRepository.findByEmail(request.email())
                        .orElseThrow(()-> {
                            logger.warn("Login attempted for non-existing user: {}", request.email());
                            return new RuntimeException("User not found.");
                        });

        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User logged in: {}", user.getEmail());

        return new LoginResponse(user.getEmail(), token);
    }


    /**
     * Sends a password reset token to the user's email
     * @param email
     */
    public void sendPasswordResetToken(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->
                        {
                            logger.warn("Password reset requested for non-existent email: {}", email);
                            return new UsernameNotFoundException("No user found with email: " + email);
                        });

        //invalidate old token if any
        passwordResetTokenRepository.deleteByUser(user);

        //generate new token and expiry
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);

        passwordResetTokenRepository.save(resetToken);

        //send email (mock)
        String resetLink = "http://localhost:80080/api/auth/reset-password?token="+ token;
        emailService.sendEmail(user.getEmail(), "Password Reset Request", "[MOCK EMAIL] To reset your password, click this link: " + resetLink);

        logger.info("Password reset token sent to: {}", user.getEmail());
    }


    /**
     * Resets the password to a new password if valid token is provided.
     * @param token
     * @param newPassword
     */
    public void resetPassword(String token, String newPassword){
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()-> {
                    logger.warn("Invalid password reset token used: {}", token);
                    return new IllegalArgumentException("Invalid password reset token.");
                });

        if(resetToken.isExpired()){
            logger.warn("Expired password rest token attempted: {}", token);
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        logger.info("Password successfully reset for user: {}", user.getEmail());
    }

}
