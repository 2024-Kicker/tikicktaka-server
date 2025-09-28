package com.example.tikicktaka.web.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class TeamResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamDetailDTO {
        Long teamId;
        String teamName;
        String teamInfo;
        String stadiumName;
        String location;
        String logoUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamImgResultDTO {
        String teamName;
        String logoUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamImgUploadResultDTO {
        String teamName;
        LocalDateTime createdAt;
        String logoUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamPreviewListDTO {
        List<TeamPreviewDTO> teamPreviewDTOList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamPreviewDTO {
        Long teamId;
        String teamName;
        String location;
        String stadiumName;
        String logoUrl;
        LocalDateTime createdAt;
    }
}
