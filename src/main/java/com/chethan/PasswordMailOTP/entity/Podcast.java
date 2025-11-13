package com.chethan.PasswordMailOTP.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "podcasts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String author;
    private String authorName;
    private String category;
    private String imageUrl;
    @Column(unique = true)
    private String rssUrl;
    private LocalDateTime lastUpdated;
    private String sourceType;

    private Long views = (long) 0;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaybackHistory> playbackHistories;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites;

    @ManyToMany(mappedBy = "podcasts",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Playlist> playlists;


    @Column(name = "duration")
    private Double duration;

    public void incrementViews(){
        if(this.views==null){
            this.views=0L;
        }
        this.views++;
    }


//    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Episode> episodes =new ArrayList<>();
}
