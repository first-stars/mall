package com.w.gulimall.thirdparty.service.impl;

import com.w.common.utils.R;
import com.w.gulimall.thirdparty.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * @author xin
 * @date 2023-02-07-21:05
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public R sendSimpleEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            System.out.println("验证码发送成功");
            return R.ok();
        } catch (MailException e) {
            System.out.println("验证码发送成功 " + e.getMessage());
            e.printStackTrace();
            return R.error();
        }
    }
}
