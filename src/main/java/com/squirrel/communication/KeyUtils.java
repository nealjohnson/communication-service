package com.squirrel.communication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyUtils {

    private static final Log logger = LogFactory.getLog(KeyUtils.class);

    public static PrivateKey readKeyFile(String filePath) throws IOException, PKCSException, NoSuchAlgorithmException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());
        File file = ResourceUtils.getFile(filePath);
        logger.info(String.format("path is %s  and %s and %s ", filePath ,file.exists(), file));
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
}
