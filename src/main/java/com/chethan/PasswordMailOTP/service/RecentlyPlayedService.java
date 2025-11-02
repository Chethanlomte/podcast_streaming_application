package com.chethan.PasswordMailOTP.service;

import com.chethan.PasswordMailOTP.entity.PlaybackHistory;
import com.chethan.PasswordMailOTP.dto.RecentlyPlayedDTO;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.PlaybackHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecentlyPlayedService {

    @Autowired
    private PlaybackHistoryRepo playbackHistoryRepo;

    public List<RecentlyPlayedDTO> getRecentlyPlayedByUser(User user) {
        List<PlaybackHistory> historyList = playbackHistoryRepo.findTop10ByUserOrderByLastUpdatedDesc(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        return historyList.stream().map(history -> {
            var podcast = history.getPodcast();
            return new RecentlyPlayedDTO(
                    podcast.getId(),
                    podcast.getTitle(),
                    podcast.getImageUrl(),  // Assuming your Podcast entity has imageUrl field
                    history.getPosition(),
                    history.getProgress(),
                    history.getLastUpdated().format(formatter)
            );
        }).collect(Collectors.toList());
    }
}
