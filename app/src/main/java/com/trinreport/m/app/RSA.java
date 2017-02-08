package com.trinreport.m.app;

import android.util.Base64;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;


//https://vshivam.wordpress.com/2015/06/09/android-javascript-and-python-compatible-rsa-encryption/

public class RSA {

    private static String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";

    private static String RSA_CONFIGURATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static String RSA_PROVIDER = "BC";

    public static KeyPair generateRsaKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    public static String createStringFromPublicKey(Key publicKey) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        return PUBLIC_KEY_HEADER + new String(Base64.encode(x509EncodedKeySpec.getEncoded(), Base64.NO_WRAP), "UTF-8")
                + PUBLIC_KEY_FOOTER;
    }

    public static String encryptRsa(Key key, String clearText) throws Exception {
        Cipher c = Cipher.getInstance(RSA_CONFIGURATION, RSA_PROVIDER);
        c.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT));
        byte[] encodedBytes = Base64.encode(c.doFinal(clearText.getBytes("UTF-8")), Base64.DEFAULT);
        String cipherText = new String(encodedBytes, "UTF-8");
        return cipherText;
    }

    public static String decrypt(String cipherText, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] decryptedBytes;
        String decrypted;
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = cipher.doFinal(stringToBytes(cipherText));
        decrypted = new String(decryptedBytes);
        return decrypted;
    }

    public static PublicKey createPublicKeyFromString(String publicKeyString) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKeyString = publicKeyString.replace(PUBLIC_KEY_HEADER, "");
        publicKeyString = publicKeyString.replace(PUBLIC_KEY_FOOTER, "");
        return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.NO_WRAP)));
    }

    private static String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    private  static byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }
}