package com.squirrel.communication;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyUtils {

    private static final Log logger = LogFactory.getLog(KeyUtils.class);

    private static String keyPath ="classpath:certificates/key.pem";
    private static String certificatePath ="classpath:certificates/cert.pem";

    public static PrivateKey readKeyFile(String filePath) throws IOException, PKCSException, NoSuchAlgorithmException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());
        File file = ResourceUtils.getFile(filePath);
        PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(file)));
        KeyFactory factory = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
        byte[] content = pemReader.readPemObject().getContent();
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }

    public static X509Certificate readCertificate(String filePath) throws CertificateException, FileNotFoundException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        File file = ResourceUtils.getFile(filePath);
        FileInputStream is = new FileInputStream(file);
        X509Certificate cer = (X509Certificate) certFactory.generateCertificate(is);
        return cer;
    }

    //TODO this works but needs to be refactored.
    public static void signPdf(File file) throws IOException, com.itextpdf.text.DocumentException, GeneralSecurityException, PKCSException {
        logger.info(String.format("Start : Signing the pdf file %s", file.getName()));
        PrivateKey privateKey = KeyUtils.readKeyFile(keyPath);
        X509Certificate certificate = KeyUtils.readCertificate(certificatePath);

        Document doc = new Document();
        File outFile = new File("test");
        PdfWriter writer = PdfWriter.getInstance(doc,new FileOutputStream(outFile));
        doc.open();
        PdfReader reader = new PdfReader(new FileInputStream(file));
        int numberOfPages = reader.getNumberOfPages();
        logger.info(String.format("Total no of pages %s", numberOfPages));

        System.out.println();
        for (int i = 1; i <= numberOfPages; i++) {
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
        appearance.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, "SignatureField");
        ExternalSignature pks = new PrivateKeySignature(privateKey, "SHA512", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, new X509Certificate[]{certificate}, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
        logger.info(String.format("End : Signing the pdf file successful %s", file.getName()));

    }
}
