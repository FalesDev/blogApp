package com.falesdev.blog.service;

import com.falesdev.blog.exception.EmailException;
import com.falesdev.blog.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String testEmail = "user@example.com";
    private final String testName = "Test User";

    @Test
    @DisplayName("Successful mail sending")
    void sendWelcomeEmail_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String expectedHtml = "<html>Welcome</html>";
        when(templateEngine.process(eq("welcome-email"), any(Context.class)))
                .thenReturn(expectedHtml);

        emailService.sendWelcomeEmail(testEmail, testName);

        verify(mailSender).send(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("welcome-email"), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertEquals(testName, context.getVariable("name"));
        assertEquals("logo.png", context.getVariable("imageResourceName"));
    }

    @Test
    @DisplayName("Error sending email throws EmailException")
    void sendWelcomeEmail_MessagingException_ThrowsEmailException() throws Exception {
        MimeMessage mockedMimeMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Simulated error"))
                .when(mockedMimeMessage).setSubject(anyString(), anyString());

        when(mailSender.createMimeMessage()).thenReturn(mockedMimeMessage);
        assertThrows(EmailException.class, () -> {
            emailService.sendWelcomeEmail(testEmail, testName);
        });
        verify(mockedMimeMessage).setSubject(
                eq("¡Bienvenido a CyberBlog!"),
                eq("UTF-8")
        );
    }
}
