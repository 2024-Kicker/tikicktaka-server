package com.example.tikicktaka.domain.matches;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_schedule")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "match_field")
    private String matchField;

    @Column(name = "home_team")
    private String homeTeam;

    @Column(name = "away_team")
    private String awayTeam;

    @Column(name = "match_date_time")
    private LocalDateTime matchDateTime;

    @Column(name = "score")
    private String score;

    @Column(nullable = false)
    private Boolean matchStatus;
}

