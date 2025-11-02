package com.chethan.PasswordMailOTP.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PlaybackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playbackId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;

    private Double progress;    //percentage 0.0 to 100.0
    private Long position;      //current position in sec or ms

    private LocalDateTime lastUpdated;

    private Boolean completed;

}
