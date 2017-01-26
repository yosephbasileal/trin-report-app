package com.trinreport.m.app.utils;

import java.util.regex.PatternSyntaxException;

/**
 * Created by bimana2 on 11/5/16.
 */
public class Utilities {
    public static boolean validate_email(String email, String domain) {
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
}
