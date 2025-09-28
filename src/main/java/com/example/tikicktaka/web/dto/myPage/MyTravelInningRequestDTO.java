package com.example.tikicktaka.web.dto.myPage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class MyTravelInningRequestDTO {

    @Getter
    @Setter
    public static class Create {
        @NotNull
        private LocalDate date;

        @NotNull
        private Long opponentTeamId;

        @Size(max = 300)
        private String content;
    }
}
