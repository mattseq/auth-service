package com.mattseq.authservice.controller;

import com.mattseq.authservice.domain.Role;
import com.mattseq.authservice.domain.User;
import com.mattseq.authservice.dto.CreateUserRequest;
import com.mattseq.authservice.dto.UserResponse;
import com.mattseq.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
public class DevController {
    private final UserService userService;

    public DevController(UserService userService) {
        this.userService = userService;
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
}
