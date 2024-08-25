package com.example.tikicktaka.web.dto.member;

import com.example.tikicktaka.domain.enums.LanTourCategory;
import com.example.tikicktaka.domain.images.LanTourImg;
import com.example.tikicktaka.web.dto.lanTour.LanTourResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MemberResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResultDTO{

        Long memberId;
        String nickname;
        String email;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class memberProfileDTO{
        Long memberId;
        String email;
        String nickname;
        String teamName;
        Long point;
        String phoneNumber;
        String gender;
        String profileImgUrl;
        List<Boolean> memberTerm;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileResultDTO{

        Long memberId;
        String nickname;
        String email;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResultDTO{
        String jwt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginIdDuplicateConfirmResultDTO{
        Boolean checkLoginId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NicknameDuplicateConfirmResultDTO{
        Boolean checkNickname;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAuthSendResultDTO{
        String email;
        String authCode;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAuthConfirmResultDTO{
        Boolean checkEmail;
        String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteSignupResultDTO {
        private Long id;
        private String nickname;
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLoginResponseDTO {
        private Long memberId;
        private String nickName;
        private String email;
        private String jwt;
        private String message;
    }
  
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileModifyResultDTO {
        String nickname;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPreferTeamDTO{
        Long memberTeamId;
        LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchIdDTO{
        private Long id;
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterSellerResultDTO{
        private String nickname;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModifySellerResultDTO{
        private String nickname;
        private String email;
        private String memberRole;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChargeCoinResultDTO{
        private String nickname;
        private Long point;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseLanTourDetailDTO{
        private String memberName;
        private String lanTourTitle;
        private Long price;
        private Long salesCount;
        private String location;
        private String lanTourCategory;
        private List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImgList;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DibsLanTourPreviewDTO{
        private Long lanTourId;
        private String lanTourTitle;
        private Long price;
        private String lanTourCategory;
        private List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImgList;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DibsLanTourPreviewListDTO{
        List<DibsLanTourPreviewDTO> dibsLanTourPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseLanTourPreviewDTO{
        private Long purchaseLanTourId;
        private String lanTourTitle;
        private Long price;
        private String lanTourCategory;
        private List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImgList;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseLanTourPreviewListDTO{
        List<PurchaseLanTourPreviewDTO> purchaseLanTourPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LanTourDibsResultDTO{
        Long lanTourId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LanTourDibsDeleteDTO{
        Long lanTourId;
        Long memberId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeCoinPreviewDTO{
        Long amount;
        String title;
        Long leftover;
        LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeCoinPreviewListDTO{
        Long amount;
        List<ChargeCoinPreviewDTO> chargeCoinPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendCoinPreviewDTO{
        Long price;
        String title;
        LocalDateTime createdAt;
        List<LanTourResponseDTO.LanTourImageResponseDTO> lanTourImgList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendCoinPreviewListDTO{
        Long amount;
        List<SpendCoinPreviewDTO> spendCoinPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyReviewPreviewDTO{
        Long lanTourId;
        String nickname;
        String contents;
        Double ratings;
        String profileImgUrl;
        LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  MyReviewPreviewListDTO{
        List<MyReviewPreviewDTO> myReviewPreviewDTOList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }
}
