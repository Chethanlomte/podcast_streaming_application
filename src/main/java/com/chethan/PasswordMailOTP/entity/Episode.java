package com.chethan.PasswordMailOTP.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//@Table(name = "episodes", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"podcast_id", "audioUrl"})
//})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @ManyToMany
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;

    private String audioUrl;
    private LocalDateTime publishDate;
    private Integer guid;
    private Integer durationSeconds;
}
