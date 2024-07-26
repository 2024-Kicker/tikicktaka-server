package com.example.tikicktaka.converter.team;

import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.images.TeamImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import com.example.tikicktaka.web.dto.team.TeamResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class TeamConverter {

    public static TeamResponseDTO.TeamDetailDTO toTeamDetailDTO(Team team){
        return TeamResponseDTO.TeamDetailDTO.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .teamInfo(team.getTeamInfo())
                .location(team.getLocation())
                .build();
    }

    public static TeamImg toTeamImg(String logo, String stadium, Team team){
        return TeamImg.builder()
                .logoUrl(logo)
                .stadiumUrl(stadium)
                .team(team)
                .build();
    }

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
                .logoUrl(team.getTeamImg().getLogoUrl())
                .stadiumUrl(team.getTeamImg().getStadiumUrl())
                .createdAt(team.getCreatedAt())
                .build();
    }

    public static TeamResponseDTO.TeamPreviewListDTO teamPreviewListDTO(List<Team> teamList){

        List<TeamResponseDTO.TeamPreviewDTO> teamPreviewDTOList = teamList.stream()
                .map(TeamConverter::teamPreviewDTO).collect(Collectors.toList());

        return TeamResponseDTO.TeamPreviewListDTO.builder()
                .teamPreviewDTOList(teamPreviewDTOList)
                .build();
    }
}
