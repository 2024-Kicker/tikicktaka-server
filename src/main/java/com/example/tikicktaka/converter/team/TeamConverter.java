package com.example.tikicktaka.converter.team;

import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.team.TeamResponseDTO;

public class TeamConverter {

    public static TeamResponseDTO.TeamDetailDTO toTeamDetailDTO(Team team){
        return TeamResponseDTO.TeamDetailDTO.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .teamInfo(team.getTeamInfo())
                .location(team.getLocation())
                .build();
    }
}
