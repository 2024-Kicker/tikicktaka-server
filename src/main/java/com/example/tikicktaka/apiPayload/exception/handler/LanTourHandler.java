package com.example.tikicktaka.apiPayload.exception.handler;

import com.example.tikicktaka.apiPayload.code.BaseErrorCode;
import com.example.tikicktaka.apiPayload.exception.GeneralException;

public class LanTourHandler extends GeneralException {
    public LanTourHandler(BaseErrorCode code) {
        super(code);
    }
}
