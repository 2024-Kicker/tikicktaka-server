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

    // Term
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "TERM4001", "해당 약관이 존재하지 않습니다.");


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
