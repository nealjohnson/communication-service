package com.squirrel.communication.service;


import com.lowagie.text.DocumentException;
import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.impl.EmailTypeEnum;
import org.joda.time.DateTime;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public abstract class EmailService {

     private JavaMailSender sender;
     private TemplateEngine templateEngine;

     protected EmailService(JavaMailSender sender, TemplateEngine templateEngine) {
          this.sender = sender;
          this.templateEngine = templateEngine;
     }

     public  void  sendEmail(EmailModel emailModel) throws MessagingException, IOException, DocumentException {

         final Context ctx = populateContext(emailModel);
         MimeMessage message = sender.createMimeMessage();
         MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                 StandardCharsets.UTF_8.name());
         helper.setTo(emailModel.getAddresseEmailAddress());
         helper.setSubject(emailModel.getSubject());
         String pdfContent;
         String emailText;
         if(emailModel.getEmailType().equals(EmailTypeEnum.PRESCRIPTION)){
             pdfContent =   templateEngine.process(emailModel.getEmailTemplateFile(), ctx);
             File file = generatePdf("Squirrel-" + DateTime.now().millisOfDay(), pdfContent);
             helper.addAttachment(file.getName(),file);
             emailText = templateEngine.process("html/email-general-template.html", ctx);
         }else {
             emailText =   templateEngine.process(emailModel.getEmailTemplateFile(), ctx);
         }
         helper.setText(emailText, true);
         sender.send(message);
    }

    public abstract Context populateContext(EmailModel emailModel);

     private File generatePdf(String fileName, String html) throws IOException, DocumentException {
         final File outputFile = File.createTempFile(fileName, ".pdf");

         FileOutputStream os = new FileOutputStream(outputFile);
         ITextRenderer renderer = new ITextRenderer();
         renderer.setDocumentFromString(html);
         renderer.layout();
         renderer.createPDF(os);
         renderer.finishPDF();
         return outputFile;
     }
}
