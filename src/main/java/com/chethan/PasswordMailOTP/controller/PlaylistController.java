package com.chethan.PasswordMailOTP.controller;

import com.chethan.PasswordMailOTP.dto.PlaylistDTO;
import com.chethan.PasswordMailOTP.entity.Playlist;
import com.chethan.PasswordMailOTP.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/playlist")
public class PlaylistController {


    @Autowired
    private PlaylistService playlistService;

    @PostMapping("/create/{user_id}")
    public ResponseEntity<PlaylistDTO> createPlaylist(@PathVariable Long user_id, @RequestBody Map<String, String> body){
        return ResponseEntity.ok(playlistService.createPlaylist(user_id, body.get("name")));
    }

    @PostMapping("/add/{playlist_id}/{podcast_id}")
    public ResponseEntity<Playlist> addToPlaylist(@PathVariable Long playlist_id, @PathVariable Long podcast_id){
        return ResponseEntity.ok(playlistService.addToPlaylist(playlist_id, podcast_id));
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getPlaylistByUser(@PathVariable Long user_id){
        return ResponseEntity.ok(playlistService.getPlaylistByUser(user_id));
    }
}
