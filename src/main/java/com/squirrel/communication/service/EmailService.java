package com.squirrel.communication.service;


import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import com.squirrel.communication.KeyUtils;
import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.service.impl.EmailTypeEnum;
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


public abstract class EmailService {

    private JavaMailSender sender;
    private TemplateEngine templateEngine;

    private String keyPath ="classpath:certificates/key.pem";
    private String certificatePath ="classpath:certificates/cert.pem";

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
            File file = generatePdf("Squirrel-" + DateTime.now().millisOfDay(), pdfContent);
            try {
               signPdf(file);
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

    public void signPdf(File file) throws IOException, com.itextpdf.text.DocumentException, GeneralSecurityException, PKCSException {

        PrivateKey privateKey = KeyUtils.readKeyFile(keyPath);
        X509Certificate certificate = KeyUtils.readCertificate(certificatePath);


        Document doc = new Document();
        File outFile = new File("test");
        PdfWriter writer = PdfWriter.getInstance(doc,new FileOutputStream(outFile));
        doc.open();
        PdfReader reader = new PdfReader(new FileInputStream(file));
        int n;
        n = reader.getNumberOfPages();
        System.out.println("No. of Pages :" +n);
        for (int i = 1; i <= n; i++) {
            if (i == 1) {
                Rectangle rect = new Rectangle(85, 650, 800, 833);
                PdfFormField pushButton = PdfFormField.createPushButton(writer);
                pushButton.setWidget(rect, PdfAnnotation.HIGHLIGHT_PUSH);
                PdfContentByte cb = writer.getDirectContent();
                PdfAppearance app = cb.createAppearance(380, 201);
                app.rectangle(62, 100, 50, -1);
                app.fill();
                pushButton.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, app);
                writer.addAnnotation(pushButton);
                PdfImportedPage page = writer.getImportedPage(reader, i);
                Image instance = Image.getInstance(page);
                doc.add(instance);
            }
        }

        OutputStream os = new FileOutputStream(file);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
        appearance.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, "anil");
        ExternalSignature pks = new PrivateKeySignature(privateKey, "SHA512", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, new X509Certificate[]{certificate}, null, null, null, 0, MakeSignature.CryptoStandard.CMS);


    }


}
