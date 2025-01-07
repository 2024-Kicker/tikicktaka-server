package com.example.tikicktaka.service.KBOmatchService;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.converter.team.TeamConverter;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.team.TeamResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@Tag(name = "Match", description = "KBO 경기 일정 크롤링 관련 API")
public class GameScheduleCrawlerService {

    @Autowired
    private GameScheduleService gameScheduleService;

    // 1월부터 12월까지의 모든 경기 일정을 크롤링하는 엔드포인트
    @GetMapping("/crawlAllMatches")
    public String crawlAllMatches() {
        gameScheduleService.crawlAndSaveGameScheduleForAllMatches();
        return "All game schedules from January to December have been saved!";
    }
}
