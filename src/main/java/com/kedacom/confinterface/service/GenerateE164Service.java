package com.kedacom.confinterface.service;

import java.util.regex.Pattern;

public class GenerateE164Service {

    public static void InitE164(String e164StartString) {
        lastE164 = e164StartString;
        numeric = isNumericString(lastE164);
        e164Len = e164StartString.length();
        if (numeric) {
            /*long的最大值为9223372036854775807,长度超过e164号规定的16位长度*/
            lastE164long = Long.parseLong(lastE164);
        } else {
            lastE164long = 0;
        }
    }

    public static String generateE164() {

        if (numeric) {
            lastE164long++;
            lastE164 = String.format("%0"+e164Len+"d", lastE164long);
            return lastE164;
        }

        if (null == vmtE164Start) {
            vmtE164Start = new StringBuilder(lastE164);
        }

        int pos = vmtE164Start.length() - 1; //从最后一位依次向前检测
        while (pos >= 0) {
            if (!Character.isDigit(vmtE164Start.charAt(pos))) {
                pos--;
                continue;
            }

            int value = vmtE164Start.charAt(pos) - '0';
            if (9 == value) {
                vmtE164Start.setCharAt(pos, '0');
                pos--;
                continue;
            }

            value++;
            char valueChar = (char) (value + '0');
            vmtE164Start.setCharAt(pos, valueChar);

            return vmtE164Start.toString();
        }

        return null;
    }

    private static boolean isNumericString(String inString) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(inString).matches();
    }

    private static StringBuilder vmtE164Start;
    private static boolean numeric;
    private static String lastE164;
    private static int e164Len;
    private static long lastE164long;
}
