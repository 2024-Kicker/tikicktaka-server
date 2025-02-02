package com.example.tikicktaka.web.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameScheduleRequestDTO {

    @Schema(description = "경기 날짜", example = "2025-06-15")
    private String matchDate;

    @Schema(description = "홈 팀")
    private String homeTeam;

    @Schema(description = "어웨이 팀")
    private String awayTeam;
}

