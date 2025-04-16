package com.ffaustin.job_tracker.controller;

import com.ffaustin.job_tracker.dto.ApiResponse;
import com.ffaustin.job_tracker.dto.UpdatePasswordRequest;
import com.ffaustin.job_tracker.dto.UserResponse;
import com.ffaustin.job_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserInfo() throws AccessDeniedException {

        UserResponse response =  userService.getCurrentUser();

        return ResponseEntity.ok(ApiResponse.of(200, "User Info", response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request){
        userService.updatePassword(request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.of(200, "Password changed", "Your password has been updated."));
    }

}
