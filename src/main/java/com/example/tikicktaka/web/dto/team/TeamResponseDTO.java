package com.example.tikicktaka.web.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TeamResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDetailDTO{
        Long teamId;
        String teamName;
        String teamInfo;
        String location;
    }
}
