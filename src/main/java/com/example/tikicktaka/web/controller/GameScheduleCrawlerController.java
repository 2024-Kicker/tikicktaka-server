package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.converter.gameSchedule.GameScheduleConverter;
import com.example.tikicktaka.domain.gameSchedule.GameSchedule;
import com.example.tikicktaka.service.KBOmatchService.GameScheduleService;
import com.example.tikicktaka.web.dto.match.GameScheduleRequestDTO;
import com.example.tikicktaka.web.dto.match.GameScheduleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/match")
@Tag(name = "Match", description = "KBO 경기 일정 관련 API")
public class GameScheduleCrawlerController {

    @Autowired
    private GameScheduleService gameScheduleService;

    // 1월부터 12월까지의 모든 경기 일정을 크롤링하는 엔드포인트
    @GetMapping("/crawlAllMatches")
    @Operation(summary = "KBO 경기 일정 저장 API", description = "KBO 경기 일정 데이터베이스에 저장")
    public String crawlAllMatches() {
        gameScheduleService.crawlAndSaveGameScheduleForAllMatches();
        return "All game schedules from January to December have been saved!";
    }

    // KBO 경기 일정 출력 API
    @GetMapping("/schedules")
    @Operation(summary = "KBO 경기 일정 조회 API", description = "데이터베이스에 저장된 모든 KBO 경기 일정 조회")
    public List<String> getAllSchedules() {
        List<GameSchedule> schedules = gameScheduleService.getAllGameSchedules();

        return schedules.stream()
                .map(schedule -> String.format("%s: %s vs %s  |  경기장:  %s (%s)",
                        schedule.getMatchDateTime().toString(), // 경기 날짜 및 시간
                        schedule.getAwayTeam(), // 어웨이 팀
                        schedule.getHomeTeam(), // 홈 팀
                        schedule.getMatchField(), // 경기 구장
                        schedule.getMatchStatus() ? "경기 완료" : "경기 예정"))
                .collect(Collectors.toList());
    }

    // 특정 경기 정보 조회 API
    // 특정 경기 정보 조회 (JSON 요청)
    @PostMapping("/searchMatch")
    @Operation(summary = "특정 경기 정보 조회", description = "request: 날짜, 홈팀, 어웨이팀")
    public ApiResponse<GameScheduleResponseDTO> getSpecificGameSchedule(@RequestBody GameScheduleRequestDTO request) {
        String matchDate = request.getMatchDate();
        String homeTeam = request.getHomeTeam();
        String awayTeam = request.getAwayTeam();

        if (matchDate == null || homeTeam == null || awayTeam == null) {
            return ApiResponse.onFailure(ErrorStatus.GAME_SCHEDULE_REQUIRED_FIELDS_MISSING.getCode(),
                    ErrorStatus.GAME_SCHEDULE_REQUIRED_FIELDS_MISSING.getMessage(), null);
        }
        GameSchedule gameSchedule = gameScheduleService.findGameScheduleByDateAndTeams(
                LocalDate.parse(matchDate), homeTeam, awayTeam);

        if (gameSchedule != null) {
            GameScheduleResponseDTO responseDTO = GameScheduleConverter.toGameScheduleResponseDTO(gameSchedule);
            return ApiResponse.onSuccess(responseDTO);
        } else {
            return ApiResponse.onFailure(ErrorStatus.GAME_SCHEDULE_NOT_FOUND.getCode(),
                    ErrorStatus.GAME_SCHEDULE_NOT_FOUND.getMessage(), null);
        }
    }

    // 팀별 다음 경기 일정 반환 API
    //JSON 입력으로 팀 이름을 받아 다음 경기 반환
    @GetMapping("/nextMatch")
    @Operation(summary = "특정 팀 다음 경기 정보 조회", description = "parameter: 팀 이름 간략하게 (두산, 키움, 롯데...) ")
    public ApiResponse<GameScheduleResponseDTO> getNextMatch(@RequestParam String teamName) {
        Optional<GameSchedule> nextMatch = gameScheduleService.findNextMatchByTeamName(teamName);

        if (nextMatch.isPresent()) {
            GameSchedule gameSchedule = nextMatch.get();
            GameScheduleResponseDTO responseDTO = new GameScheduleResponseDTO(
                    gameSchedule.getMatchDate(),
                    gameSchedule.getHomeTeam(),
                    gameSchedule.getAwayTeam(),
                    gameSchedule.getMatchField()
            );

            return ApiResponse.onSuccess(responseDTO);
        } else {
            return ApiResponse.onFailure(ErrorStatus.GAME_SCHEDULE_NOT_FOUND.getCode(),
                    ErrorStatus.GAME_SCHEDULE_NOT_FOUND.getMessage(), null);
        }
    }
}
