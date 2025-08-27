package com.example.tikicktaka.web.dto.home;

import java.time.LocalDate;
import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class HomeHeaderResponseDTO {
    private String nickname;
    private LocalDate gameDate;      // 예: 2024-10-27 (표시는 프론트 포맷)
    private String baseTeamName;     // 기준 구단명
    private String opponentTeamName; // 상대 팀명
    private String stadiumName;      // 구장 이름
}
