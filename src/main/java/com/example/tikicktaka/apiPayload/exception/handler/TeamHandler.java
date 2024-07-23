package com.example.tikicktaka.apiPayload.exception.handler;

import com.example.tikicktaka.apiPayload.code.BaseErrorCode;
import com.example.tikicktaka.apiPayload.exception.GeneralException;

public class TeamHandler extends GeneralException {
    public TeamHandler(BaseErrorCode code) {
        super(code);
    }
}
