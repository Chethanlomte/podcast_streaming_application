package com.chethan.PasswordMailOTP.dto;


import com.chethan.PasswordMailOTP.entity.Playlist;
import com.chethan.PasswordMailOTP.entity.Podcast;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistDTO {

    private Long playlistId;
    private String name;
    private Long userId;
    private List<Long> podcasts;
}
