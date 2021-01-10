package com.squirrel.communication.service.impl;

import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;

@Component
public class EmailContext {

    private JavaMailSender sender;

    private TemplateEngine templateEngine;


    @Autowired
    public EmailContext(JavaMailSender sender, TemplateEngine templateEngine) {
        this.sender = sender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailModel emailInfoModel , EmailTypeEnum emailTypeEnum ) throws MessagingException {
        EmailService emailService;
        switch (emailTypeEnum){
            case ACCOUNT:
                emailService = new AccountCreationEmailService(sender,templateEngine);
                break;
            case APPOINTMENT:
                emailService = new AppointmentCreationEmailService(sender,templateEngine);
                break;
            case PRESCRIPTION:
                emailService = new PrescriptionCreationEmailService(sender,templateEngine);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + emailTypeEnum);
        }
        emailService.sendEmail(emailInfoModel);
    }
}
