package com.ffaustin.job_tracker.services;

import com.ffaustin.job_tracker.dto.UserResponse;
import com.ffaustin.job_tracker.entity.User;
import com.ffaustin.job_tracker.repository.UserRepository;
import com.ffaustin.job_tracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void getCurrentUser_shouldReturnUserResponse_whenUserIsAuthenticated() throws Exception{
        User user = User.builder()
                .firstName("Fabrice")
                .lastName("Faustin")
                .email("fab@example.com")
                .build();

        UserDetails mockPrincipal = mock(UserDetails.class);
        when(mockPrincipal.getUsername()).thenReturn(user.getEmail());

        //set the mock principal in the security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockPrincipal, null)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser();

        assertEquals("fab@example.com", response.email());
        assertEquals("Fabrice", response.firstName());
        assertEquals("Faustin", response.lastName());
    }

    @Test
    void getCurrentUser_shouldThrowAccessDenied_whenPrincipalIsNotUserDetails(){
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null)
        );

        assertThatThrownBy(()-> userService.getCurrentUser())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Unauthorized");
    }

    @Test
    void getCurrentUser_shouldThrowUsernameNotFound_whenUserNotFoundInRepository(){
        String emailNotInDatabase = "missing@example.com";

        UserDetails mockPrincipal = mock(UserDetails.class);

        when(mockPrincipal.getUsername()).thenReturn(emailNotInDatabase);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockPrincipal, null)
        );

        when(userRepository.findByEmail(emailNotInDatabase)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updatePassword_shouldUpdatePassword_ifValid(){
        User user = User.builder()
                .email("fab@example.com")
                .password("hashed-password")
                .build();

        UserDetails mockPrincipal = mock(UserDetails.class);

        when(mockPrincipal.getUsername()).thenReturn(user.getEmail());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockPrincipal, null)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("new-hashed");

        userService.updatePassword("Password123", "NewPassword456");

        assertEquals("new-hashed", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_shouldThrow_whenCurrentPasswordIsIncorrect(){
        User user = User.builder()
                .email("fab@example.com")
                .password("hashed-password")
                .build();

        UserDetails mockPrincipal = mock(UserDetails.class);
        when(mockPrincipal.getUsername()).thenReturn(user.getEmail());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockPrincipal, null)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(()-> userService.updatePassword("wrong-password", "NewPassword456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password provided is incorrect.");

    }
}
