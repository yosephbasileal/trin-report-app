package com.trinreport.m.app;

import android.util.Base64;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;


public class AES {

    private static String KEY_GENERATION_PASSWORD = "B2XqMWtvhRjKv2a2EpwiRLObnvIV3PmKKFGjB0bG";

    // generates aes key based on a password
    public static byte[] generateKey() {
        try {
            SecureRandom random = new SecureRandom();
            int iterationCount = 1000;
            int keyLength = 256;
            int saltLength = keyLength / 8; // same size as key output
            byte[] salt = new byte[saltLength];
            random.nextBytes(salt);
            KeySpec keySpec = new PBEKeySpec(KEY_GENERATION_PASSWORD.toCharArray(), salt,
                    iterationCount, keyLength);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

            return keyBytes;
        } catch(Exception e) {

        }
        return null;
    }

    // generates iv for aes
    public static byte[] generateIV() {
        try  {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[cipher.getBlockSize()];
            random.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            return iv;
        } catch (Exception e) {

        }
        return null;
    }

    // Takes key and iv and encryptes plain text to a cipher text
    public static String encryptAES(SecretKey key, byte[] iv, String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] encodedBytes = Base64.encode(
                    cipher.doFinal(plaintext.getBytes("UTF-8")), Base64.DEFAULT);
            String cipherText = new String(encodedBytes, "UTF-8");
            return cipherText;
        } catch (Exception e) {

        }
        return null;
    }
}
