package com.gitkey.build.crypto;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static byte[] digest(byte[] data, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not found: " + algorithm, e);
        }
    }

    public static byte[] digest(InputStream inputStream, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }
        return md.digest();
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String toHexWithColons(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    public static String sha256(byte[] data) {
        return toHexString(digest(data, "SHA-256"));
    }

    public static String sha1(byte[] data) {
        return toHexString(digest(data, "SHA-1"));
    }

    public static String md5(byte[] data) {
        return toHexString(digest(data, "MD5"));
    }

    public static String sha256Fingerprint(byte[] certBytes) {
        return toHexWithColons(digest(certBytes, "SHA-256"));
    }

    public static String sha1Fingerprint(byte[] certBytes) {
        return toHexWithColons(digest(certBytes, "SHA-1"));
    }

    public static String md5Fingerprint(byte[] certBytes) {
        return toHexWithColons(digest(certBytes, "MD5"));
    }
}
