package com.mattseq.authservice.controller;

import com.mattseq.authservice.domain.Role;
import com.mattseq.authservice.domain.User;
import com.mattseq.authservice.dto.CreateUserRequest;
import com.mattseq.authservice.dto.LoginRequest;
import com.mattseq.authservice.dto.UserResponse;
import com.mattseq.authservice.service.JwtService;
import com.mattseq.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/initialize")
    public ResponseEntity<UserResponse> initializeAdmin(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(Role.ADMIN)
                .build();

        return userService.initializeAdmin(user)
                .map(savedUser -> ResponseEntity.ok(UserResponse.fromUser(savedUser)))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(Role.USER)
                .build();

        return userService.createUser(user)
                .map(u -> ResponseEntity.ok(UserResponse.fromUser(u)))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        ResponseEntity<UserResponse> user = userService.login(request.getUsername(), request.getPassword())
            .map(u -> ResponseEntity.ok(
                    UserResponse.fromUser(u)
            ))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());


        if (user.getStatusCode() == HttpStatus.OK) {
            String token = jwtService.generateToken(Objects.requireNonNull(user.getBody()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(user.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/auth/verify")
    public ResponseEntity<UserResponse> verify(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long id = (Long) authentication.getPrincipal();
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(UserResponse.fromUser(user)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/auth/demo-register")
    public ResponseEntity<UserResponse> registerDemo(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(Role.ADMIN)
                .build();

        return userService.createUser(user)
                .map(u -> ResponseEntity.ok(UserResponse.fromUser(u)))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/ping")
    public String adminPing() {
        return "pong";
    }
}
