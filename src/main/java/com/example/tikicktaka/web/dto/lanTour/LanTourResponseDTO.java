package com.example.tikicktaka.web.dto.lanTour;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class LanTourResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourImageResponseDTO {
        Boolean isThumbNail;
        String lanTourImgUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourDetailResponseDTO{
        String title;
        String contents;
        Long price;
        List<LanTourImageResponseDTO> lanTourImgList;
        Long salesCount;
        Long dibsCount;
        String location;
        String lanTourCategory;
        LocalDateTime createdAt;
    }
}
