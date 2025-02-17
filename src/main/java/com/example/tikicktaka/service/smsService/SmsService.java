package com.example.tikicktaka.service.smsService;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private final DefaultMessageService messageService;

    @Value("${COOLSMS_NUMBER}")
    private String fromPhoneNumber;

    public SmsService(@Value("${COOLSMS_API_KEY}") String apiKey,
                      @Value("${COOLSMS_SECRET_KEY}") String apiSecret) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public void sendSms(String to, String verificationCode) {
        Message message = new Message();

        message.setFrom(fromPhoneNumber); // CoolSMS에서 등록한 발신번호
        message.setTo(to);
        message.setText("[트래블이닝] 본인인증 코드: " + verificationCode);

        messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}

