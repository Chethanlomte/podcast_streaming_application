package com.chethan.PasswordMailOTP.service;

import com.chethan.PasswordMailOTP.dto.PlaylistDTO;
import com.chethan.PasswordMailOTP.entity.Playlist;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import com.chethan.PasswordMailOTP.repository.PlaylistRepo;
import com.chethan.PasswordMailOTP.repository.PodcastRepo;
import com.chethan.PasswordMailOTP.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PodcastRepo podcastRepo;

    @Autowired
    private PlaylistRepo playlistRepo;

    // Convert Playlist to PlaylistDTO
    public PlaylistDTO toDTO(Playlist playlist){
        return PlaylistDTO.builder()
                .playlistId(playlist.getId())
                .name(playlist.getName())
                .userId(playlist.getUser().getId())
                .podcasts(
                        playlist.getPodcasts() != null
                                ? playlist.getPodcasts().stream().map(Podcast::getId).toList()
                                : List.of()
                )
                .build();
    }

    public PlaylistDTO createPlaylist(Long userId, String name) {
        User user = userRepo.findById(userId).
                orElseThrow(()-> new RuntimeException("User Not Found"));
        Playlist playlist = Playlist.builder()
                .user(user)
                .name(name)
                .build();
        Playlist saved =  playlistRepo.save(playlist);
        return toDTO(saved);

    }

    public Playlist addToPlaylist(Long playlist_id, Long podcast_id) {
        Playlist playlist = playlistRepo.findById(playlist_id)
                .orElseThrow(()-> new RuntimeException("Playlist Not Found"));
        Podcast podcast = podcastRepo.findById(podcast_id)
                .orElseThrow(()-> new RuntimeException("Podcast Not Found"));

        playlist.getPodcasts().add(podcast);
        return playlistRepo.save(playlist);
    }

    //    Get all playlists created by a user
    public List<Playlist> getPlaylistByUser(Long user_id) {
        User user = userRepo.findById(user_id)
                .orElseThrow(()-> new RuntimeException("User Not Found"));
        return playlistRepo.findByUser(user);
    }
}
