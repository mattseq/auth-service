package com.mattseq.authservice.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthServiceController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthServiceController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/register")
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword()) // TODO: hash this shit now before it hits the fan
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
                    .build()
            ))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());


        if (user.getStatusCode() == HttpStatus.OK) {
            String token = jwtService.generateToken(user.getBody().getId().toString());

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

    @GetMapping("/auth/logout")
    public String logout() {
        return "logout";
    }

    @GetMapping("/auth/verify")
    public String verify() {
        return "verify";
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
