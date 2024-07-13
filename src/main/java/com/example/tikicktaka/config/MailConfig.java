package com.example.tikicktaka.config;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailConfig {
    private final JavaMailSender javaMailSender;

    public boolean sendMail(String ToEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setFrom(new InternetAddress("tikicktaka@gmail.com", "Tikicktaka", "UTF-8"));
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject("Tikicktaka 인증번호 "); // 제목
            helper.setTo(ToEmail); // 받는사람

            String verificationCode = code;
            String emailBody = String.format("Tikicktaka 인증번호는 %s 입니다.", verificationCode);

            helper.setText(emailBody, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
