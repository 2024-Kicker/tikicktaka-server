package com.example.tikicktaka.web.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteSignupResultDTO {
        private Long id;
        private String name;
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLoginResponseDTO {
        private Long memberId;
        private String nickname;
        private String email;
        private String jwt;
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
}
