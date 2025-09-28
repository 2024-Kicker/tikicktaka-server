package com.example.tikicktaka.converter.team;

import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.team.TeamResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class TeamConverter {

    public static TeamResponseDTO.TeamDetailDTO toTeamDetailDTO(Team team){
        return TeamResponseDTO.TeamDetailDTO.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .teamInfo(team.getTeamInfo())
                .stadiumName(team.getStadiumName())
                .location(team.getLocation())
                .logoUrl(team.getLogoUrl())
                .build();
    }

    // 팀 이미지 엔티티(TeamImg) 생성 메서드 제거 ✅
    // public static TeamImg toTeamImg(...) 필요 없음

    public static TeamResponseDTO.TeamImgUploadResultDTO teamImgUploadResultDTO(Team team) {
        return TeamResponseDTO.TeamImgUploadResultDTO.builder()
                .teamName(team.getTeamName())
                .createdAt(team.getCreatedAt())
                .build();
    }

    public static TeamResponseDTO.TeamPreviewDTO teamPreviewDTO(Team team){
        return TeamResponseDTO.TeamPreviewDTO.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .location(team.getLocation())
                .stadiumName(team.getStadiumName())
                .logoUrl(team.getLogoUrl())
                .createdAt(team.getCreatedAt())
                .build();
    }

    public static TeamResponseDTO.TeamPreviewListDTO teamPreviewListDTO(List<Team> teamList){
        List<TeamResponseDTO.TeamPreviewDTO> teamPreviewDTOList = teamList.stream()
                .map(TeamConverter::teamPreviewDTO)
                .collect(Collectors.toList());

        return TeamResponseDTO.TeamPreviewListDTO.builder()
                .teamPreviewDTOList(teamPreviewDTOList)
                .build();
    }
}
