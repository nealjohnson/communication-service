package com.squirrel.communication.service.impl;

import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Locale;

public class AccountCreationEmailService extends EmailService {

    public AccountCreationEmailService(JavaMailSender sender, TemplateEngine templateEngine) {
        super(sender, templateEngine);
    }

    @Override
    public Context populateContext(EmailModel emailModel){
        emailModel.setEmailTemplateFile("html/email-account-template.html");
        final Context ctx = new Context(Locale.getDefault());
        ctx.setVariable("addresseName", emailModel.getAddresseName());
        ctx.setVariable("linkToBeShown", emailModel.getLinkToBeShown());
        ctx.setVariable("imageResourceName", emailModel.getImageResource());
       return ctx;
    }
}
