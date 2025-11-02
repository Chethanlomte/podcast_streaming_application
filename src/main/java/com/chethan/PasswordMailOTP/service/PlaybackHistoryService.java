package com.chethan.PasswordMailOTP.service;


import com.chethan.PasswordMailOTP.entity.PlaybackHistory;
import com.chethan.PasswordMailOTP.dto.PlaybackHistoryDTO;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.PlaybackHistoryRepo;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlaybackHistoryService {


    @Autowired
    PlaybackHistoryRepo playbackHistoryRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    PodcastRepo podcastRepo;

    public PlaybackHistoryDTO syncPlayback(Long userId, Long podcastId, Double progress, Long position) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Podcast podcast = podcastRepo.findById(podcastId)
                .orElseThrow(() -> new RuntimeException("Podcast not found"));

//      Existing playback record or create a new one
        PlaybackHistory history = playbackHistoryRepo.findByUserAndPodcast(user, podcast)
                .orElse(PlaybackHistory.builder()
                        .user(user)
                        .podcast(podcast)
                        .progress(0.0)
                        .position(0L)
                        .completed(false)
                        .build());

        double newProgress;
        //Auto-calculate progress if podcast has duration
        if (podcast.getDuration() != null && podcast.getDuration() > 0) {
            newProgress = (position / podcast.getDuration()) * 100.0;
        } else {
            newProgress = progress != null ? progress : 0.0; // fallback
        }

        //If playback is complete (user watched full podcast)
        if (newProgress >=99.9){
            history.setProgress(100.0);
            history.setPosition(0L);
            history.setCompleted(true);
        }else {
//            Update playback progress & mark not completed
            history.setProgress(progress);
            history.setPosition(position);
            history.setCompleted(false);
        }

        history.setLastUpdated(LocalDateTime.now());
        PlaybackHistory saved = playbackHistoryRepo.save(history);


        return PlaybackHistoryDTO.builder()
                .playbackId(saved.getPlaybackId())
                .userId(userId)
                .podcastId(podcastId)
                .progress(saved.getProgress())
                .position(saved.getPosition())
                .lastUpdated(saved.getLastUpdated())
                .build();

    }

    public PlaybackHistoryDTO getPlayback(Long userId, Long podcastId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Podcast podcast = podcastRepo.findById(podcastId)
                .orElseThrow(() -> new RuntimeException("Podcast not found"));

        PlaybackHistory history = playbackHistoryRepo.findByUserAndPodcast(user, podcast)
                .orElse(PlaybackHistory.builder()
                        .user(user)
                        .podcast(podcast)
                        .progress(0.0)
                        .position(0L)
                        .completed(false)
                        .lastUpdated(LocalDateTime.now())
                        .build());



//        If the last playback was completed, start from 0
        if (Boolean.TRUE.equals(history.getCompleted()))
        {
            history.setProgress(0.0);
            history.setPosition(0L);
            history.setCompleted(false);
            history.setLastUpdated(LocalDateTime.now());
            playbackHistoryRepo.save(history);
        }
        return PlaybackHistoryDTO.builder()
                .playbackId(history.getPlaybackId())
                .userId(userId)
                .podcastId(podcastId)
                .progress(history.getProgress())
                .position(history.getPosition())
                .lastUpdated(history.getLastUpdated())
                .build();
    }
}
