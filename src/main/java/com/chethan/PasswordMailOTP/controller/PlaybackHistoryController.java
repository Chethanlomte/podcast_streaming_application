package com.chethan.PasswordMailOTP.controller;


import com.chethan.PasswordMailOTP.service.PlaybackHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/playbackHistorySync")
public class PlaybackHistoryController {

    @Autowired
    private PlaybackHistoryService playbackHistoryService;


    // Called when user plays, pauses, or updates playback
    @PostMapping("/sync/{userId}/{podcastId}")
    public ResponseEntity<?> syncPlayback(@PathVariable Long userId, @PathVariable Long podcastId, @RequestBody Map<String, Object> body) {

        if (body.get("progress") == null || body.get("position") == null) {
            return ResponseEntity.badRequest().body("Missing 'progress' or 'position' in request body");
        }

        Double progress = Double.valueOf(body.get("progress").toString());
        Long position = Long.valueOf(body.get("position").toString());

        return ResponseEntity.ok(
                playbackHistoryService.syncPlayback(userId, podcastId, progress, position)
        );
    }

    // Called when user opens podcast again to resume
    @GetMapping("/resume/{userId}/{podcastId}")
    public ResponseEntity<?> getPlayback(@PathVariable Long userId, @PathVariable Long podcastId) {
        return ResponseEntity.ok(playbackHistoryService.getPlayback(userId, podcastId));
    }
}
