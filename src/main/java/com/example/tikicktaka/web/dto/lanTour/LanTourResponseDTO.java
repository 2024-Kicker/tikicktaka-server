package com.example.tikicktaka.web.dto.lanTour;

import com.example.tikicktaka.domain.enums.InquiryStatus;
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
        int reviewCount;
        int inquiryCount;
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
        List<LanTourPreviewDTO> lanTourPreviewDTOList;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadInquiryAnswerResultDTO{
        Long memberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourReviewPreviewDTO{
        String memberNickname;
        String profileImgUrl;
        String contents;
        Double ratings;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourReviewPreviewListDTO{
        List<LanTourReviewPreviewDTO> lanTourReviewPreviewDTOList;
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
    public static class LanTourInquiryPreviewDTO{
        String memberNickname;
        String profileImgUrl;
        String title;
        String contents;
        String inquiryStatus;
        Boolean secret;
        LanTourResponseDTO.LanTourInquiryAnswerPreviewDTO lanTourInquiryAnswer;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanTourInquiryPreviewListDTO{
        List<LanTourInquiryPreviewDTO> lanTourInquiryPreviewDTOList;
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
    public static class LanTourInquiryAnswerPreviewDTO{
        String memberNickname;
        String profileImgUrl;
        String contents;
        LocalDateTime createdAt;
    }
}
