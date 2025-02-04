package com.example.tikicktaka.web.dto.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
public class GameScheduleResponseDTO {
    private LocalDate matchDate;
    private String homeTeam;
    private String awayTeam;
    private String score;
    private String matchField;

    public GameScheduleResponseDTO(LocalDate matchDate, String homeTeam, String awayTeam, String score, String matchField) {
        this.matchDate = matchDate;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.matchField = matchField;
    }

    public GameScheduleResponseDTO(LocalDate matchDate, String homeTeam, String awayTeam, String matchField) {
        this.matchDate = matchDate;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchField = matchField;
    }
}
