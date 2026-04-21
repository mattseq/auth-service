package com.mattseq.authservice.controller;

import com.mattseq.authservice.dto.UpdateUserRequest;
import com.mattseq.authservice.dto.UserResponse;
import com.mattseq.authservice.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = (Long) authentication.getPrincipal();
        UserResponse userResponse = userService.findById(id)
                .map(UserResponse::fromUser)
                .orElse(null);
        return ResponseEntity.ok(userResponse);
    }

    // TODO: VULNERABILITY: Allowing users to update their own role could lead to privilege escalation. Do not allow role changes.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody UpdateUserRequest updateUserRequest) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = (Long) authentication.getPrincipal();

        return userService.updateUser(id, updateUserRequest)
                .map(updatedUser -> ResponseEntity.ok(UserResponse.fromUser(updatedUser)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(UserResponse.fromUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(id, updateUserRequest)
                .map(updatedUser -> ResponseEntity.ok(UserResponse.fromUser(updatedUser)))
                .orElse(ResponseEntity.notFound().build());
    }
}
