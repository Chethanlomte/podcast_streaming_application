package com.chethan.PasswordMailOTP.repository;

import com.chethan.PasswordMailOTP.entity.Playlist;
import com.chethan.PasswordMailOTP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepo extends JpaRepository<Playlist, Long> {


    List<Playlist> findByUser(User user);
}
