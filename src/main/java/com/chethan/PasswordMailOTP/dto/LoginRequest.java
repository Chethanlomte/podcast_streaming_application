package com.chethan.PasswordMailOTP.dto;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Builder
public class LoginRequest {
    private String email;
    private String password;
}
