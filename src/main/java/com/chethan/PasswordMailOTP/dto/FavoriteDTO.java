package com.chethan.PasswordMailOTP.dto;

import com.chethan.PasswordMailOTP.entity.Favorite;
import com.chethan.PasswordMailOTP.entity.Podcast;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

public class FavoriteDTO {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favid;
    private Podcast podcast;

    public FavoriteDTO(Favorite favorite){
        this.favid=favorite.getId();
        this.podcast=favorite.getPodcast();
    }
}
