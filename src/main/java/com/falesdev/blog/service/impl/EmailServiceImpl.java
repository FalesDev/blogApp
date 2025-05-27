package com.falesdev.blog.service.impl;

import com.falesdev.blog.exception.EmailException;
import com.falesdev.blog.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    //Funciona bien el mailSender, warning es por el IntelliJ.
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    @Override
    public void sendWelcomeEmail(String to, String name) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("cyberblogapp@gmail.com");
            helper.setTo(to);
            helper.setSubject("¡Bienvenido a CyberBlog!");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("imageResourceName", "logo.png");
            String html = templateEngine.process("welcome-email", context);

            helper.setText(html, true);

            ClassPathResource logo = new ClassPathResource("static/logo.png");
            helper.addInline("logo", logo);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailException("Error sending welcome email");
        }
    }
}
