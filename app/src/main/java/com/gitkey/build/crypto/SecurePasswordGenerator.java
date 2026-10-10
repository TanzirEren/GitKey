package com.gitkey.build.crypto;

import java.security.SecureRandom;

public class SecurePasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generatePassword(int length) {
        if (length < 8) length = 16;
        StringBuilder sb = new StringBuilder(length);

        // Guarantee at least one of each category
        sb.append(UPPER.charAt(secureRandom.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(secureRandom.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(secureRandom.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            sb.append(ALL_CHARS.charAt(secureRandom.nextInt(ALL_CHARS.length())));
        }

        // Shuffle securely
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    public static String generateDefault() {
        return generatePassword(16);
    }
}
