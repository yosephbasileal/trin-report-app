package com.trinreport.m.app;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


//source: http://stackoverflow.com/questions/12471999/rsa-encryption-decryption-in-android

public class RSA {

    public static KeyPair generateRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static String encrypt(String plainText, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] encryptedBytes;
        String encrypted;
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = cipher.doFinal(plainText.getBytes());
        encrypted = bytesToString(encryptedBytes);
        return encrypted;

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