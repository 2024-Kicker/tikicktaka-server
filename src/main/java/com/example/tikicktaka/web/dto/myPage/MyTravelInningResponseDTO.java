package com.example.tikicktaka.web.dto.myPage;

import com.fasterxml.jackson.core.JsonToken;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class MyTravelInningResponseDTO {

    @Getter
    @Setter
    @Builder
    public static class Detail {
        private Long id;
        private LocalDate date;
        private String imageUrl;
        private Long preferTeamId;
        private String preferTeamImageUrl;
        private Long opponentTeamId;
        private String opponentTeamImageUrl;
        private String content;

    }
}
