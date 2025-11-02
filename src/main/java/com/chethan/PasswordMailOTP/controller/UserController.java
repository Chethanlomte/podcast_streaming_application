package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.config.JwtTokenProvider;
import com.chethan.PasswordMailOTP.dto.ApiResponse;
import com.chethan.PasswordMailOTP.dto.JwtAuthResponse;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

@Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody User userRequest) {
        try {
            // Input validation
            if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
            }
            
            if (userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password is required"));
            }
            
            if (userRequest.getName() == null || userRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Name is required"));
            }
            
            if (userRequest.getGender() == null || userRequest.getGender().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Gender is required"));
            }
            
            if (userRequest.getPhoneNumber() == null || userRequest.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Phone number is required"));
            }
            
            // Email format validation
            if (!userRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email format"));
            }
            
            // Password strength validation (at least 6 characters, 1 number, 1 special char)
            if (!userRequest.getPassword().matches("^(?=.*[0-9])(?=.*[!@#$%^&*])(?=\\S+$).{6,}$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password must be at least 6 characters long and contain at least one number and one special character"));
            }
            
            // Phone number validation (10 digits)
            if (!userRequest.getPhoneNumber().matches("^\\d{10}$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Phone number must be 10 digits long"));
            }
            
            ApiResponse response = userService.registerUser(
                userRequest.getEmail().trim(),
                userRequest.getPassword(),
                userRequest.getName().trim(),
                userRequest.getGender().trim(),
                userRequest.getPhoneNumber().trim()
            );
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("An error occurred during registration: " + e.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        System.out.println("Login attempt for email: " + (loginRequest != null ? loginRequest.getEmail() : "null"));
        try {
            // Input validation
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is required"));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password is required"));
            }
            
            // Check if user exists first
            Optional<User> userOptional = userService.getUserByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
            }
            
            // Try to authenticate
            try {
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail().trim(),
                        loginRequest.getPassword()
                    )
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Generate JWT token
                String token = tokenProvider.generateToken(loginRequest.getEmail());
                
                // Get user details without password
                User user = userOptional.get();
                user.setPassword(null);
                
                // Create response
                JwtAuthResponse response = JwtAuthResponse.builder()
                    .token(token)
                    .user(user)
                    .message("Login successful")
                    .build();
                
                return ResponseEntity.ok(response);
                
            } catch (BadCredentialsException e) {
                System.out.println("Bad credentials for user: " + loginRequest.getEmail());
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
            } catch (Exception authException) {
                System.err.println("Authentication error: " + authException.getMessage());
                authException.printStackTrace();
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication failed: " + authException.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("An error occurred during login. Please try again later."));
        }
    }
    @PostMapping("/profile")
    public ResponseEntity<?> viewProfile(@RequestBody User requestUser){
        Optional<User> user=userService.getUserByEmail(requestUser.getEmail());
        if(user.isPresent()){
            return ResponseEntity.ok(user.get());
        }
        else{
            return ResponseEntity.status(404).body("User Not Found");
        }
    }

    @PostMapping("/profileUpdate")
    public ResponseEntity<User> updateProfile(@RequestBody User updatedUser){
        Optional<User> user = userService.updateUser(updatedUser.getEmail(), updatedUser);
//        if(user.isPresent()){
        return user.map(ResponseEntity::ok)
                    .orElseGet(()->ResponseEntity.status(404).build());
//            return ResponseEntity.ok(user.get());
//        }else{
//            return ResponseEntity.status(404).body("User Not Found");
//        }
    }

    @PostMapping("/getPremium")
    public ResponseEntity<?> getPremium(@RequestBody User requestUser){
        Optional<User> optionalUser = userService.getUserByEmail(requestUser.getEmail());

        if(optionalUser.isEmpty()){
            return ResponseEntity.status(404).body("User not Found with email : "+requestUser.getEmail());
        }

        String duration = requestUser.getDuration();
        if (
                !duration.equals("30Days") &&
                        !duration.equals("90Days") &&
                        !duration.equals("180Days") &&
                        !duration.equals("365Days")
        ){
            return ResponseEntity.badRequest()
                    .body("Invalid Package Choosen. Available Packages are 30Day, 90Day, 180Days, 365Day ");
        }
        User upgradeUser = userService.upgradeToPremium(optionalUser.get().getId(), duration);
        return ResponseEntity.ok(requestUser);
    }
}
