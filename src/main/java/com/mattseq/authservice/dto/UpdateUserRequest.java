package com.mattseq.authservice.dto;

import com.mattseq.authservice.domain.Role;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;

    @Email
    private String email;

    private String password;

    private Role role;
}
