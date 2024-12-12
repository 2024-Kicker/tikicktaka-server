package com.example.tikicktaka.domain.matches;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "matches")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_field")
    private String matchField;

    @Column(name = "home_team")
    private String homeTeam;

    @Column(name = "away_team")
    private String awayTeam;

    @Column(name = "match_date_time")
    private LocalDateTime matchDateTime; // 경기 시작 시간 (날짜 + 시간)

    @Column(name = "score")
    private String score; //경기 스코어

    @Column(nullable = false)
    private Boolean matchStatus = false;  // 경기 상태 필드 (True=오늘 경기, False=다른 날)

}
