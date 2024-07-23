package com.example.tikicktaka.web.dto.member;

import com.example.tikicktaka.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

public class MemberRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinDTO{

        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotBlank(message = "이름을 입력해주세요")
        private String name;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요.")
        private String password;

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        private Date birthday;

        @NotBlank(message = "휴대폰 번호를 입력해주세요")
        private String phone;

        private Gender gender;

        private List<Boolean> memberTerm;
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

}
