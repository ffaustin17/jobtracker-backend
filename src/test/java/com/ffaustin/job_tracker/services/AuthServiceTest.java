package com.ffaustin.job_tracker.services;

import com.ffaustin.job_tracker.dto.LoginRequest;
import com.ffaustin.job_tracker.dto.LoginResponse;
import com.ffaustin.job_tracker.dto.RegisterRequest;
import com.ffaustin.job_tracker.dto.ResetPasswordRequest;
import com.ffaustin.job_tracker.entity.PasswordResetToken;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.entity.VerificationToken;
import com.ffaustin.job_tracker.repository.PasswordResetTokenRepository;
import com.ffaustin.job_tracker.repository.UserRepository;
import com.ffaustin.job_tracker.repository.VerificationTokenRepository;
import com.ffaustin.job_tracker.service.AuthService;
import com.ffaustin.job_tracker.service.EmailService;
import com.ffaustin.job_tracker.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserAndSendVerificationToken() {
        //setup
        RegisterRequest request = new RegisterRequest(
                "Fabrice",
                "Faustin",
                "fab@example.com",
                "Password123"
        );


        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPass");

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("fab@example.com"), anyString());
    }

    @Test
    void shouldThrowIfUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Fabrice",
                "Faustin",
                "fab@example.com",
                "Password123"
        );

        //simulate existing user in database
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered.");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    @Test
    void shouldLoginSuccessfullyAndReturnJwt() {
        LoginRequest request = new LoginRequest("fab@example.com", "Password123");

        // returns an Authentication object if credentials are correct
        Authentication fakeAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(fakeAuth);

        //simulate user retrieval
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setEnabled(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        when(jwtUtil.generateToken(user.getEmail())).thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.email()).isEqualTo("fab@example.com");
        assertThat(response.token()).isEqualTo("mock-jwt-token");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("fab@example.com");
        verify(jwtUtil).generateToken("fab@example.com");
    }

    @Test
    void login_shouldThrowIfUserNotFound() {
        LoginRequest request = new LoginRequest("missing@example.com", "Password123");

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found.");
    }

    @Test
    void sendPasswordResetToken_shouldSendEmailIfUserExists() {
        User user = User.builder()
                .email("fab@example.com")
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        authService.sendPasswordResetToken(user.getEmail());

        verify(passwordResetTokenRepository).save(any());
    }

    @Test
    void sendPasswordResetToken_shouldThrowIfUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.sendPasswordResetToken("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("No user found with email: " + "unknown@example.com");
    }

    @Test
    void resetPassword_shouldUpdatePasswordIfTokenIsValid(){
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUser(new User());

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");

        authService.resetPassword(request.token(), request.newPassword());

        verify(userRepository).save(token.getUser());
    }

    @Test
    void resetPassword_shouldThrowIfTokenInvalidOrExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setExpiryDate(LocalDateTime.now().minusMinutes(10));

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "newPass");

        assertThatThrownBy(() -> authService.resetPassword(request.token(), request.newPassword()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password reset token has expired.");
    }

}

