package com.example.tikicktaka.web.dto.lanTour;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LanTourRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadReviewRequestDTO{
        private String contents;
        private Double ratings;
    }
}
