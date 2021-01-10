package com.squirrel.communication.service;


import com.squirrel.communication.model.EmailModel;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public abstract class EmailService {

     private JavaMailSender sender;
     private TemplateEngine templateEngine;

     protected EmailService(JavaMailSender sender, TemplateEngine templateEngine) {
          this.sender = sender;
          this.templateEngine = templateEngine;
     }

     public  void  sendEmail(EmailModel emailModel) throws MessagingException {
         MimeMessage message = sender.createMimeMessage();
         MimeMessageHelper helper = new MimeMessageHelper(message);
         helper.setTo(emailModel.getAddresseEmailAddress());
         helper.setSubject(emailModel.getSubject());
         final Context ctx = populateContext(emailModel);
         final String htmlContent = templateEngine.process(emailModel.getEmailTemplateFile(), ctx);
         MimeMultipart ma = new MimeMultipart("alternative");
         message.setContent(ma);
         // create the html part
         BodyPart html = new MimeBodyPart();
         html.setContent(htmlContent, "text/html");
         ma.addBodyPart(html);
         sender.send(message);
    }

    public abstract Context populateContext(EmailModel emailModel);

}
