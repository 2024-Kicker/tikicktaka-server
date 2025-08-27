package com.example.tikicktaka.web.dto.home;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@AllArgsConstructor
public class NextGameSliceDTO {
    private LocalDateTime gameDateTime;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private String stadiumName;
}

