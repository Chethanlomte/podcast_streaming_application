package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.dto.FavoriteDTO;
import com.chethan.PasswordMailOTP.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {


    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{userId}/{podcastId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long userId, @PathVariable Long podcastId){
        return ResponseEntity.ok(favoriteService.toggleFavorite(userId, podcastId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<List<FavoriteDTO>> getFavorite(@PathVariable Long userId){
        return ResponseEntity.ok(favoriteService.getFavorite(userId));
    }
}
