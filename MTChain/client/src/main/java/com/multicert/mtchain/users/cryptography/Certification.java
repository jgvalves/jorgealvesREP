package com.multicert.mtchain.users.cryptography;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Certification {

        private static final String SIGNING_ALGORITHM = "SHA1withRSA";

        private static PrivateKey privateKey;
        private static Certificate certificate;

        private Certification(){}

        public static String convertX509toBase64(X509Certificate certificate) {

            try {
                BASE64Encoder encoder = new BASE64Encoder();

                byte[] derCert = certificate.getEncoded();
                return new String(encoder.encode(derCert));
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        public static X509Certificate certificateFromByteStream(byte[] byteStream) throws IOException, CertificateException{
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(byteStream);
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);

            return cert;
        }

    public static X509Certificate certificateFromFile(File certificate) throws IOException, CertificateException{
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        Path fileLocation = Paths.get(certificate.getAbsolutePath());
        InputStream in = new ByteArrayInputStream(Files.readAllBytes(fileLocation));
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);

        return cert;
    }

        //TODO message type; DEFINE SIGNING ALGORITHM
        public static boolean verifySignature(byte[] message, byte[] signedMessage, PublicKey signerPubKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initVerify(signerPubKey);
            signature.update(message);
            return signature.verify(signedMessage);
        }

        public static byte[] performSignature(String message, PrivateKey signerKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initSign(signerKey);
            signature.update(message.getBytes());
            return signature.sign();
        }

    public static String computeHash(MultipartFile document) throws IOException, NoSuchAlgorithmException{
        byte[] data = document.getBytes();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return new BASE64Encoder().encode(hash);

    }

    public static void setKeyPair(File keystoreFile) throws Exception{
        FileInputStream is = new FileInputStream(keystoreFile);
        KeyStore keystore = KeyStore.getInstance("JKS");

        String password = "changeit";
        String alias = "sercer";

        keystore.load(is, password.toCharArray());
        privateKey = (PrivateKey) keystore.getKey(alias, "changeit".toCharArray());
        certificate = keystore.getCertificate(alias);
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        Certification.privateKey = privateKey;
    }

    public static Certificate getCertificate() {
        return certificate;
    }

    public static void setCertificate(Certificate certificate) {
        Certification.certificate = certificate;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {

        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
