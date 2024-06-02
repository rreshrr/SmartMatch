package ru.alfastudents.smartmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EmailService {

    @Autowired
    private final JavaMailSender emailSender;

    @Value("${app.email.whitelist}")
    private final List<String> allowedEmails;

    public void sendMessage(String to, String subject, String text) {
        if (allowedEmails.contains(to)) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        }
    }
}