package com.chethan.PasswordMailOTP.dto;

import com.chethan.PasswordMailOTP.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String token;
    private User user;
    private String message;
}
