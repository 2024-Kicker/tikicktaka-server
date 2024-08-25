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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadInquiryRequestDTO{
        private String title;
        private String contents;
        private Boolean secret;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadInquiryAnswerRequestDTO{
        private String contents;
    }
}
