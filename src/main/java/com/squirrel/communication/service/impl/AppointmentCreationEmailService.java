package com.squirrel.communication.service.impl;

import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.EmailService;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

public class AppointmentCreationEmailService extends EmailService {

    public AppointmentCreationEmailService(JavaMailSender sender, TemplateEngine templateEngine) {
        super(sender, templateEngine);
    }

    @Override
    public Context populateContext(EmailModel emailModel){
        emailModel.setEmailTemplateFile("html/email-appointment-template.html");
        final Context ctx = new Context(Locale.getDefault());
        ctx.setVariable("addresseName", emailModel.getAddresseName());
        ctx.setVariable("linkToBeShown", emailModel.getLinkToBeShown());
        DateTimeFormatter builder = DateTimeFormat.forPattern("dd-MM-yyyy hh:mm.SSa");
        ctx.setVariable("appointmentDateTime", builder.print(emailModel.getAppointmentDateTime()));
        ctx.setVariable("imageResourceName", emailModel.getImageResource()); // so that we can reference it from HTML
        ctx.setVariable("senderName", emailModel.getSenderName());
        return ctx;
    }
}
