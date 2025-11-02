package com.chethan.PasswordMailOTP.dto;


import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PlaybackHistoryDTO {

    private Long playbackId;
    private Long userId;
    private Long podcastId;
    private Double progress;
    private Long position;
    private LocalDateTime lastUpdated;

}
