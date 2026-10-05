package com.example.tab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * 发送普通文本邮件
     */
    public void sendSimpleMail(
            String to,
            String subject,
            String content) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("2889541533@qq.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }
}