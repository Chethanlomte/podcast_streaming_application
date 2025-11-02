package com.chethan.PasswordMailOTP.dto;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecentlyPlayedDTO {
    private Long podcastId;
    private String title;
    private String thumbnail;
    private Long position;
    private Double progress;
    private String lastPlayed;
}
