package com.tvboot.tivio.dto.auth;

import com.tvboot.tivio.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private boolean isActive;
}