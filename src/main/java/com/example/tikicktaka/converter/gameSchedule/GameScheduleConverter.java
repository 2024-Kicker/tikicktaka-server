package com.example.tikicktaka.converter.gameSchedule;

import com.example.tikicktaka.domain.gameSchedule.GameSchedule;
import com.example.tikicktaka.web.dto.match.GameScheduleResponseDTO;

public class GameScheduleConverter {

    // GameSchedule -> GameScheduleResponseDTO 변환 메서드
    public static GameScheduleResponseDTO toGameScheduleResponseDTO(GameSchedule gameSchedule) {
        return new GameScheduleResponseDTO(
                gameSchedule.getMatchDate(),
                gameSchedule.getHomeTeam(),
                gameSchedule.getAwayTeam(),
                gameSchedule.getScore(),
                gameSchedule.getMatchField()
        );
    }
}