package com.example.tikicktaka.web.dto.member;

import com.example.tikicktaka.domain.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public class MemberRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinDTO{

        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요.")
        private String password;

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        private Date birthday;

//        @NotBlank(message = "휴대폰 번호를 입력해주세요")
//        private String phone; //본인인증으로 변경하고 저장하지는 말기

        private Gender gender;

        private String introduceMessage;

        private List<Boolean> memberTerm;

        //프로필 이미지
        @Setter
        @JsonIgnore
        private MultipartFile profileImg;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMemberDTO{
        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotBlank(message = "휴대폰 번호를 입력해주세요")
        private String phone;

        private Date birthday;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterSellerDTO{
        private String title;
        private String contents;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLoginDTO{
        private String email;
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NicknameDuplicateConfirmDTO{
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailDuplicateConfirmDTO{
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsAuthDTO {
        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsAuthConfirmDTO{
        private String phoneNumber;
        private String code;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAuthDTO{
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAuthConfirmDTO{
        private String email;
        private String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteSignupDTO{
        @NotBlank(message = "생년월일을 입력해주세요")
        private Date birthday;

        @NotBlank(message = "휴대폰 번호를 입력해주세요")
        private String phone;

        //private Gender gender;

        //private List<Boolean> memberTerm;
    }
    @Getter
    @NoArgsConstructor
    public static class SearchIdDTO {
        private String phone;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeCoinRequestDTO{
        private Long amount;
        private String title;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequestDTO{
        String email;
        String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberPreferTravelStyleDTO {
        @Size(min = 2, max = 2, message = "여행 스타일은 2개 선택해야 합니다.")
        private List<String> travelStyles; // 선택한 여행 스타일 목록
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TravelStyleDTO {
        @NotBlank(message = "첫 번째 여행 스타일을 선택해야 합니다.")
        private String styleOne; // 첫 번째 스타일

        @NotBlank(message = "두 번째 여행 스타일을 선택해야 합니다.")
        private String styleTwo; // 두 번째 스타일
    }

}
