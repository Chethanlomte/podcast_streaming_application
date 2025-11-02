package com.chethan.PasswordMailOTP.service;


import com.chethan.PasswordMailOTP.dto.ApiResponse;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse registerUser(String email, String password, String name, String gender, String phoneNumber) {
        try {
            // Check if email already exists
            if (userRepo.findByEmail(email).isPresent()) {
                return ApiResponse.error("Email already registered. Please use a different email or try logging in.");
            }

            // Additional validations (as a backup to controller validations)
            if (password.length() < 6) {
                return ApiResponse.error("Password must be at least 6 characters long");
            }
            
            if (phoneNumber == null || !phoneNumber.matches("^\\d{10}$")) {
                return ApiResponse.error("Phone number must be 10 digits long");
            }

            // Encode password before saving
            String encodedPassword = passwordEncoder.encode(password);
            User user = new User(email, encodedPassword, name, gender, phoneNumber); 
            
            // Save user
            User savedUser = userRepo.save(user);
            
            // Return success response with user details (excluding sensitive data)
            savedUser.setPassword(null); // Don't return the password
            return ApiResponse.success("Registration successful! Welcome " + name + ". You can now log in with your credentials.", savedUser);
            
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ApiResponse.error("Failed to register user. Please try again later. Error: " + e.getMessage());
        }
    }

    public ApiResponse loginUser(String email, String password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ApiResponse.error("Email is required");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return ApiResponse.error("Password is required");
            }
            
            Optional<User> optionalUser = userRepo.findByEmail(email.trim());
            
            if (optionalUser.isEmpty()) {
                // Don't reveal that the user doesn't exist (security best practice)
                return ApiResponse.error("Invalid email or password");
            }
            
            User user = optionalUser.get();
            
            // Log the stored and input password hashes for debugging
            System.out.println("Stored password hash: " + user.getPassword());
            System.out.println("Input password: " + password);
            
            // Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ApiResponse.error("Invalid email or password");
            }
            
            // Remove password before returning user data
            user.setPassword(null);
            return ApiResponse.success("Login successful", user);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("An error occurred during login: " + e.getMessage());
        }
    }
    public Optional<User> getUserByEmail(String email){
        return userRepo.findByEmail(email);
    }
    public Optional<User> updateUser(String email, User updatedUser){
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if(optionalUser.isPresent()){
            User user=optionalUser.get();

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                // Encode password before updating
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
                user.setName(updatedUser.getName());
            }
            if (updatedUser.getGender() != null && !updatedUser.getGender().isEmpty()) {
                user.setGender(updatedUser.getGender());
            }
            if (updatedUser.getPhoneNumber() != null) {
                user.setPhoneNumber(updatedUser.getPhoneNumber());
            }

            userRepo.save(user);
            return Optional.of(user);
        }
        else
            return Optional.empty();

    }

    public User upgradeToPremium(Long id, String duration) {
        User user = userRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
        user.setPremium(true);

        LocalDateTime now = LocalDateTime.now();
        user.setPremiumStartDate(now);

        switch (duration) {
            case "30Days":
                user.setPremiumEndDate(now.plusDays(30));
                break;
            case "90Days":
                user.setPremiumEndDate(now.plusDays(90));
                break;
            case "180Days":
                user.setPremiumEndDate(now.plusDays(180));
                break;
            case "365Days":
                user.setPremiumEndDate(now.plusDays(365));
                break;
            default:
                throw new IllegalArgumentException("Invalid duration: " + duration);
        }

        user.setDuration(duration);
        user.setPremium(true);
        return userRepo.save(user);
    }
}
