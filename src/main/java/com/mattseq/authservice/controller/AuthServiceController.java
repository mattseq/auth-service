package com.mattseq.authservice.controller;

import com.mattseq.authservice.domain.Role;
import com.mattseq.authservice.domain.User;
import com.mattseq.authservice.dto.CreateUserRequest;
import com.mattseq.authservice.dto.LoginRequest;
import com.mattseq.authservice.dto.UserResponse;
import com.mattseq.authservice.service.JwtService;
import com.mattseq.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class AuthServiceController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthServiceController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/initialize")
    public ResponseEntity<UserResponse> initializeAdmin(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole() != null ? request.getRole() : Role.ADMIN)
                .build();

        User savedUser = userService.initializeAdmin(user).orElse(null);

        if (savedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok(mapToResponse(savedUser));
        }
    }

    @PostMapping("/admin/register")
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        User savedUser = userService.createUser(user);

        return mapToResponse(savedUser);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        ResponseEntity<UserResponse> user = userService.login(request.getUsername(), request.getPassword())
            .map(u -> ResponseEntity.ok(
                UserResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .username(u.getUsername())
                    .role(u.getRole())
                    .build()
            ))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());


        if (user.getStatusCode() == HttpStatus.OK) {
            String token = jwtService.generateToken(Objects.requireNonNull(user.getBody()));

            ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(user.getBody());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/auth/verify")
    public ResponseEntity<UserResponse> verify(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long id = jwtService.extractId(token);
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mapToResponse(user));
    }

    private UserResponse mapToResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
