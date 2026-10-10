package com.gitkey.build.model;

import java.io.Serializable;

public class GeneratedSecrets implements Serializable {
    private String projectName;
    private String keystoreFileName;
    private String keystoreBase64;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;
    private byte[] keystoreBytes;
    private String sha256Fingerprint;
    private String sha1Fingerprint;
    private String md5Fingerprint;
    private long creationTimestamp;

    public GeneratedSecrets() {
        this.creationTimestamp = System.currentTimeMillis();
    }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getKeystoreFileName() { return keystoreFileName; }
    public void setKeystoreFileName(String keystoreFileName) { this.keystoreFileName = keystoreFileName; }

    public String getKeystoreBase64() { return keystoreBase64; }
    public void setKeystoreBase64(String keystoreBase64) { this.keystoreBase64 = keystoreBase64; }

    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

    public String getKeyAlias() { return keyAlias; }
    public void setKeyAlias(String keyAlias) { this.keyAlias = keyAlias; }

    public String getKeyPassword() { return keyPassword; }
    public void setKeyPassword(String keyPassword) { this.keyPassword = keyPassword; }

    public byte[] getKeystoreBytes() { return keystoreBytes; }
    public void setKeystoreBytes(byte[] keystoreBytes) { this.keystoreBytes = keystoreBytes; }

    public String getSha256Fingerprint() { return sha256Fingerprint; }
    public void setSha256Fingerprint(String sha256Fingerprint) { this.sha256Fingerprint = sha256Fingerprint; }

    public String getSha1Fingerprint() { return sha1Fingerprint; }
    public void setSha1Fingerprint(String sha1Fingerprint) { this.sha1Fingerprint = sha1Fingerprint; }

    public String getMd5Fingerprint() { return md5Fingerprint; }
    public void setMd5Fingerprint(String md5Fingerprint) { this.md5Fingerprint = md5Fingerprint; }

    public long getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(long creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    public String getFormattedAllSecrets() {
        return "KEYSTORE_BASE64=" + (keystoreBase64 != null ? keystoreBase64 : "") + "\n" +
               "KEYSTORE_PASSWORD=" + (keystorePassword != null ? keystorePassword : "") + "\n" +
               "KEY_ALIAS=" + (keyAlias != null ? keyAlias : "") + "\n" +
               "KEY_PASSWORD=" + (keyPassword != null ? keyPassword : "");
    }
}
