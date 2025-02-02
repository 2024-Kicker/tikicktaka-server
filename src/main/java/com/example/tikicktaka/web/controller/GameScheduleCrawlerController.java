package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.domain.matches.GameSchedule;
import com.example.tikicktaka.service.KBOmatchService.GameScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/match")
@Tag(name = "Match", description = "KBO 경기 일정 크롤링 관련 API")
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
}
