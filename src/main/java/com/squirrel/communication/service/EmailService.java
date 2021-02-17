package com.squirrel.communication.service;


import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import com.squirrel.communication.KeyUtils;
import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.impl.EmailTypeEnum;
import org.apache.commons.lang.time.DateUtils;
import org.bouncycastle.pkcs.PKCSException;
import org.joda.time.DateTime;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;


public abstract class EmailService {

    private JavaMailSender sender;
    private TemplateEngine templateEngine;



    protected EmailService(JavaMailSender sender, TemplateEngine templateEngine) {
        this.sender = sender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailModel emailModel) throws MessagingException, IOException {

        final Context ctx = populateContext(emailModel);
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        helper.setTo(emailModel.getAddresseEmailAddress());
        helper.setSubject(emailModel.getSubject());
        String pdfContent;
        String emailText;
        if (emailModel.getEmailType().equals(EmailTypeEnum.PRESCRIPTION)) {
            pdfContent = templateEngine.process(emailModel.getEmailTemplateFile(), ctx);

            File file = generatePdf("Squirrel-" + DateTime.now().toLocalTime(), pdfContent);
            try {
               KeyUtils.signPdf(file);
            }catch (Exception e){
                e.printStackTrace();
            }
            helper.addAttachment(file.getName(), file);
            emailText = templateEngine.process("html/email-general-template.html", ctx);
        } else {
            emailText = templateEngine.process(emailModel.getEmailTemplateFile(), ctx);
        }
        helper.setText(emailText, true);
        sender.send(message);
    }

    public abstract Context populateContext(EmailModel emailModel);

    private File generatePdf(String fileName, String html) throws IOException {
        final File outputFile = File.createTempFile(fileName, ".pdf");
        try(FileOutputStream os = new FileOutputStream(outputFile)){
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            renderer.finishPDF();
            return outputFile;
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }




}
