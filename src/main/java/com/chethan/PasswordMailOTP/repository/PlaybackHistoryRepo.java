package com.chethan.PasswordMailOTP.repository;

import com.chethan.PasswordMailOTP.entity.PlaybackHistory;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaybackHistoryRepo extends JpaRepository<PlaybackHistory, Long> {

    // Find playback history for a specific user and podcast
    Optional<PlaybackHistory> findByUserAndPodcast(User user, Podcast podcast);

    List<PlaybackHistory> findTop10ByUserOrderByLastUpdatedDesc(User user);

}
