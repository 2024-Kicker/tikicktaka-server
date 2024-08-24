package com.example.tikicktaka.web.dto.lanTour;

import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourPreviewDTO{
        String title;
        String location;
        Long price;
        List<LanTourImageResponseDTO> lanTourImgList;
        Long salesCount;
        Long dibsCount;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourPreviewListDTO{
        List<LanTourPreviewDTO> LanTourPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadReviewResultDTO{
        Long memberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadInquiryResultDTO{
        Long memberId;
    }
}
