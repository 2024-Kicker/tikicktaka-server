package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.TeamHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.converter.team.TeamConverter;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.service.teamService.TeamQueryService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import com.example.tikicktaka.web.dto.team.TeamResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@io.swagger.v3.oas.annotations.tags.Tag(name = "Team", description = "Team 관련 API")
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;

    @GetMapping("/{teamId}")
    @Operation(summary = "팀 상세 조회 API", description = "팀 상세 정보 조회를 위한 API이며, path variable로 입력 값을 받는다. " +
            "teamId : 조회할 상품의 id")
    @Parameters(value = {
            @Parameter(name = "teamId", description = "조회할 팀의 id 입니다.")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<TeamResponseDTO.TeamDetailDTO> teamDetailDTO(@PathVariable Long teamId){

        Team team = teamQueryService.findTeamById(teamId).orElseThrow(() -> new TeamHandler(ErrorStatus.TEAM_NOT_FOUND));
        return ApiResponse.onSuccess(TeamConverter.toTeamDetailDTO(team));
    }
}
