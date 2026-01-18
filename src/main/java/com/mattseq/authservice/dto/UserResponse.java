package com.mattseq.authservice.dto;

import com.mattseq.authservice.domain.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private Role role;
}
