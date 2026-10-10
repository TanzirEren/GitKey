package com.gitkey.build.crypto;

import android.util.Base64;
import java.nio.charset.StandardCharsets;

public class Base64Utils {

    public static String encode(byte[] data) {
        if (data == null) return "";
        try {
            return Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (Throwable t) {
            // Host JVM fallback during local unit tests
            return java.util.Base64.getEncoder().encodeToString(data);
        }
    }

    public static String encodeString(String text) {
        if (text == null) return "";
        return encode(text.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decode(String base64) throws IllegalArgumentException {
        if (base64 == null) return new byte[0];
        String sanitized = base64.replaceAll("\\s+", "");
        try {
            return Base64.decode(sanitized, Base64.DEFAULT);
        } catch (Throwable t) {
            // Host JVM fallback during local unit tests
            return java.util.Base64.getDecoder().decode(sanitized);
        }
    }

    public static String decodeToString(String base64) throws IllegalArgumentException {
        byte[] bytes = decode(base64);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
