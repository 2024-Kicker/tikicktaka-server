package com.example.tikicktaka.web.dto.lanTour;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LanTourResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourImageResponseDTO {
        Boolean isThumbNail;
        String lanTourImgUrl;
    }
}
