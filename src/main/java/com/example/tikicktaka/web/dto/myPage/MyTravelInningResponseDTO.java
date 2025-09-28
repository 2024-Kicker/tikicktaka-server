package com.example.tikicktaka.web.dto.myPage;

import com.fasterxml.jackson.core.JsonToken;
import lombok.*;

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

    @Getter @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListItem {
        private Long opponentTeamId;
        private String opponentTeamImageUrl;
        private String imageUrl;
        private LocalDate date;
    }
}
