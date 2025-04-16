package com.ffaustin.job_tracker.controller;

import com.ffaustin.job_tracker.dto.*;
import com.ffaustin.job_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest){
        authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(201, "Registration successful", "Please check your email to verify your account"));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam("token") String token){
        authService.verifyEmailToken(token);
        return ResponseEntity.ok(ApiResponse.of(200, "Account verified", "Your account is now enabled."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.of(200, "successful login", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        authService.sendPasswordResetToken(request.email());
        return ResponseEntity.ok(ApiResponse.of(200, "Reset email sent", "Check your inbox for reset link."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.of(200, "Password updated", "You can now log in with your new password."));
    }
}
