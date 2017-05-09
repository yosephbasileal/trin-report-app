package com.trinreport.m.app.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.PatternSyntaxException;

/**
 * Utility functions
 */
public class Utilities {
    /**
     * Validates trinity email address
     * @param email
     */
    public static boolean validateEmail(String email) {
        String domain = "trincoll.edu";
        String[] t;
        String delimiter = "@";

        try {
            // compare domain
            t = email.split(delimiter);
            return (t[1].equals(domain));
        } catch (PatternSyntaxException e) {
            // return false
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            // return false
            return false;
        }
    }

    public static boolean validateCode(String code) {
        int min = 0;
        int max = 999998;
        try {
            int codeInt = Integer.parseInt(code);
            return (codeInt > min) && (codeInt < max);
        } catch (Exception e) {
            return false;
        }
    }

    // source: https://www.simplifiedcoding.net/android-volley-tutorial-to-upload-image-to-server/
    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    // source: http://stackoverflow.com/questions/4457492/how-do-i-use-the-simple-http-client-in-android
    public static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
