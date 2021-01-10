package com.squirrel.communication.service.impl;


import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

public class PrescriptionCreationEmailService extends EmailService {

    public PrescriptionCreationEmailService(JavaMailSender sender, TemplateEngine templateEngine) {
        super(sender, templateEngine);
    }

    @Override
    public Context populateContext(EmailModel emailModel){

        emailModel.setEmailTemplateFile("html/email-prescription-template.html");
        final Context ctx = new Context(Locale.getDefault());
        ctx.setVariable("addresseName", emailModel.getAddresseName());
        ctx.setVariable("senderName", emailModel.getSenderName());
        ctx.setVariable("patientAge",emailModel.getAge());
        ctx.setVariable("prescription",emailModel.getPrescription());
        ctx.setVariable("senderAddress",emailModel.getSenderAddress());
        ctx.setVariable("senderPhoneNumber",emailModel.getSenderPhoneNumber());
        ctx.setVariable("expertise", emailModel.getExpertise());
        ctx.setVariable("subject",emailModel.getSubject());

        return ctx;
    }
}
