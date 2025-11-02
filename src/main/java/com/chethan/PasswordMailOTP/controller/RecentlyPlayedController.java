package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.dto.RecentlyPlayedDTO;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import com.chethan.PasswordMailOTP.service.RecentlyPlayedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/RecentPlaylist")
public class RecentlyPlayedController {

    @Autowired
    private RecentlyPlayedService recentlyPlayedService;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getRecentPlayed(@PathVariable Long userId){
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        List<RecentlyPlayedDTO> recentList = recentlyPlayedService.getRecentlyPlayedByUser(userOpt.get());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("recentlyPlayed", recentList);

        return ResponseEntity.ok(response);
    }
}
