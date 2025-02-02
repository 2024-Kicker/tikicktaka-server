package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.service.KBOmatchService.GameScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
