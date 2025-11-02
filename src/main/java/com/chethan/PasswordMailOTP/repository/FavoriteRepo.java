package com.chethan.PasswordMailOTP.repository;

import com.chethan.PasswordMailOTP.entity.Favorite;
import com.chethan.PasswordMailOTP.entity.Podcast;
import com.chethan.PasswordMailOTP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepo extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndPodcast(User user, Podcast podcast);
    boolean existsByUserAndPodcast(User user, Podcast podcast);
    void deleteByUserAndPodcast(User user, Podcast podcast);
    List<Favorite> findByUser(User user);

}
