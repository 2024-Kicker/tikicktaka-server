package com.example.tikicktaka.apiPayload.code.status;

import com.example.tikicktaka.apiPayload.code.BaseErrorCode;
import com.example.tikicktaka.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // Member
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST,"MEMBER4001","중복된 닉네임 입니다."),
    MEMBER_ID_DUPLICATED(HttpStatus.BAD_REQUEST, "MEMBER4002", "중복된 아이디 입니다."),
    MEMBER_ID_INCORRECT(HttpStatus.BAD_REQUEST, "MEMBER4003", "잘못된 아이디 입니다."),
    MEMBER_PASSWORD_NOT_EQUAL(HttpStatus.BAD_REQUEST, "MEMBER4004", "비밀번호가 일치하지 않습니다."),
    MEMBER_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4005", "이메일이 존재하지 않습니다."),
    MEMBER_EMAIL_AUTH_ERROR(HttpStatus.BAD_REQUEST, "MEMBER4006", "이메일 인증에 실패했습니다. 인증 코드와 이메일을 확인해주세요."),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4007", "존재하지 않는 회원입니다."),
    MEMBER_TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4008", "존재하지 않는 사용자 선호팀 정보입니다."),
    MEMBER_DIBS_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4009", "존재하지 않는 찜 내역입니다."),
    MEMBER_NOT_SELLER(HttpStatus.BAD_REQUEST, "MEMBER4010", "회원이 판매자가 아닙니다."),

    // Term
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "TERM4001", "해당 약관이 존재하지 않습니다."),

    //Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM4001", "존재하지 않는 팀입니다."),

    //Inquiry
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY4001", "존재하지 않는 문의입니다."),

    //LanTour
    LAN_TOUR_PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND,"LANTOUR4001","존재하지 않는 구매내역 입니다."),
    LAN_TOUR_NOT_FOUND(HttpStatus.NOT_FOUND, "LANTOUR4002", "존재하지 않는 상품입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
