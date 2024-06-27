package com.example.tikicktaka.apiPayload.exception.handler;

import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.GeneralException;

public class MemberHandler extends GeneralException {

    public MemberHandler(ErrorStatus errorCode) {
        super(errorCode);
    }
}
