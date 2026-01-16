package com.mattseq.iam.controller;

import com.mattseq.iam.domain.User;
import com.mattseq.iam.dto.CreateUserRequest;
import com.mattseq.iam.dto.LoginRequest;
import com.mattseq.iam.dto.UserResponse;
import com.mattseq.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IamController {
    private final UserService userService;

    public IamController(UserService userService) {
        this.userService = userService;
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

    @GetMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getPassword(), request.getPassword())
            .map(user -> ResponseEntity.ok(
                UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .build()
            ))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/auth/logout")
    public String logout() {
        return "logout";
    }

    @GetMapping("/auth/refesh")
    public String refresh() {
        return "refresh";
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
