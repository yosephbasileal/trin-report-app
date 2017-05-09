package com.trinreport.m.app;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;


/**
 * Wrapper methods for generating keys, encrypting, decrypting
 */
public class RSA {

    // constants
    private static String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";

    private static String PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    private static String PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";

    private static String RSA_CONFIGURATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static String RSA_PROVIDER = "BC";

    // generates a private and public rsa keypair
    public static KeyPair generateRsaKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    // takes a public key and converts to a string in pem format
    public static String createStringFromPublicKey(Key publicKey) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        return PUBLIC_KEY_HEADER + new String(Base64.encode(x509EncodedKeySpec.getEncoded(), Base64.NO_WRAP), "UTF-8")
                + PUBLIC_KEY_FOOTER;
    }

    // takes a private key and converts to a string in pem format
    public static String createStringFromPrivateKey(Key privateKey) throws Exception {
        PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        return PRIVATE_KEY_HEADER + new String(Base64.encode(pKCS8EncodedKeySpec.getEncoded(), Base64.NO_WRAP), "UTF-8")
                + PRIVATE_KEY_FOOTER;
    }

    // takes a plaintext and a public key (pem formatted string) and returns encrypted ciphertext
    public static String encrypt(String plain, String pubkeypem) throws Exception {
        return encryptRsa(createPublicKeyFromString(pubkeypem), plain);
    }

    // takes a cihertext and a private key (pem formatted string) and returns decrypted plaintext
    public static String decrypt(String cipher, String prvkeypem) throws Exception {
        return decryptRsa(createPrivateKeyFromString(prvkeypem), cipher);
    }

    // takes a plaintext and a public key object and returns encrypted ciphertext
    private static String encryptRsa(Key key, String clearText) throws Exception {
        Cipher c = Cipher.getInstance(RSA_CONFIGURATION, RSA_PROVIDER);
        c.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT));
        byte[] encodedBytes = Base64.encode(c.doFinal(clearText.getBytes("UTF-8")), Base64.DEFAULT);
        String cipherText = new String(encodedBytes, "UTF-8");
        return cipherText;
    }

    // takes a ciphertext and a private key object and returns decrypted plaintext
    private static String decryptRsa(Key key, String base64cypherText) throws Exception {
        Cipher c = Cipher.getInstance(RSA_CONFIGURATION, RSA_PROVIDER);
        c.init(Cipher.DECRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT));
        byte[] decodedBytes = c.doFinal(Base64.decode(base64cypherText.getBytes("UTF-8"), Base64.DEFAULT));
        String clearText = new String(decodedBytes, "UTF-8");
        return clearText;
    }

    // takes a string in pem format and converts it to a public key object
    private static PublicKey createPublicKeyFromString(String publicKeyString) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKeyString = publicKeyString.replace(PUBLIC_KEY_HEADER, "");
        publicKeyString = publicKeyString.replace(PUBLIC_KEY_FOOTER, "");
        return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.NO_WRAP)));
    }

    // takes a string in pem format and converts it to a private key object
    private static PrivateKey createPrivateKeyFromString(String privateKeyString) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKeyString = privateKeyString.replace(PRIVATE_KEY_HEADER, "");
        privateKeyString = privateKeyString.replace(PRIVATE_KEY_FOOTER, "");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKeyString, Base64.NO_WRAP)));
    }
}
