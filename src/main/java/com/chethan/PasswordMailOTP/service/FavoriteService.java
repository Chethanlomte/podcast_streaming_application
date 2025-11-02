package com.chethan.PasswordMailOTP.service;

import com.chethan.PasswordMailOTP.dto.FavoriteDTO;
import com.chethan.PasswordMailOTP.entity.Favorite;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.FavoriteRepo;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PodcastRepo podcastRepo;

    @Autowired
    private FavoriteRepo favoriteRepo;

    public Map<String, Object> toggleFavorite(Long userId, Long podcastId) {

        User user = userRepo.findById(userId).
                orElseThrow(() -> new RuntimeException("User Not Found"));

        Podcast podcast = podcastRepo.findById(podcastId).
                orElseThrow(() -> new RuntimeException("Podcast Not Found"));

        Optional<Favorite> existingFavorite = favoriteRepo.findByUserAndPodcast(user, podcast);
        Map<String, Object> response = new HashMap<>();

        if (existingFavorite.isPresent()) {
            favoriteRepo.delete(existingFavorite.get());
            response.put("message", "Removed from favorites");
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .podcast(podcast)
                    .build();
            favoriteRepo.save(favorite);
            response.put("message", "Added to favorites");

            // ✅ Include podcast details
            Map<String, Object> podcastData = new HashMap<>();
            podcastData.put("id", podcast.getId());
            podcastData.put("title", podcast.getTitle());
            podcastData.put("description", podcast.getDescription());
            podcastData.put("rssUrl", podcast.getRssUrl());
            podcastData.put("author", podcast.getAuthor());
            podcastData.put("category", podcast.getCategory());
            podcastData.put("imageUrl", podcast.getImageUrl());

            response.put("podcast", podcastData);
        }
        return response;
    }

    public List<FavoriteDTO> getFavorite(Long userId) {
        User user = userRepo.findById(userId).
                orElseThrow(() -> new RuntimeException("User Not found"));
        return favoriteRepo.findByUser(user)
                .stream()
                .map(FavoriteDTO::new)
                .collect(Collectors.toList());
    }
}
