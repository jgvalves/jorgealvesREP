package com.multicert.mtchain.backoffice.Cryptography;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Certification {

        private static final String SIGNING_ALGORITHM = "SHA1withRSA";

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

        //TODO message type; DEFINE SIGNING ALGORITHM
        public static boolean verifySignature(byte[] message, byte[] signedMessage, PublicKey signerPubKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initVerify(signerPubKey);
            signature.update(message);
            return signature.verify(signedMessage);
        }

        public static byte[] performSignature(byte[] message, PrivateKey signerKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            signature.initSign(signerKey);
            signature.update(message);
            return signature.sign();
        }

        public static String computeHash(MultipartFile document) throws IOException, NoSuchAlgorithmException{
            byte[] data = document.getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return new BASE64Encoder().encode(hash);

        }



}
