package com.gitkey.build.model;

import java.io.Serializable;

public class ProjectRecord implements Serializable {
    private String id;
    private String projectName;
    private String alias;
    private String fileName;
    private long creationDateMillis;
    private String keyAlgorithm;
    private String sha256Fingerprint;
    private String sha1Fingerprint;
    private String md5Fingerprint;
    private String logoPath;
    private String notes;
    private String tag;
    private int validityYears = 25;
    private String commonName;
    private String organization;
    private String country;
    private String keystoreBase64;
    private boolean favorite;

    public ProjectRecord() {}

    public ProjectRecord(String id, String projectName, String alias, String fileName,
                         long creationDateMillis, String keyAlgorithm,
                         String sha256Fingerprint, String sha1Fingerprint, String logoPath) {
        this.id = id;
        this.projectName = projectName;
        this.alias = alias;
        this.fileName = fileName;
        this.creationDateMillis = creationDateMillis;
        this.keyAlgorithm = keyAlgorithm;
        this.sha256Fingerprint = sha256Fingerprint;
        this.sha1Fingerprint = sha1Fingerprint;
        this.logoPath = logoPath;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getCreationDateMillis() { return creationDateMillis; }
    public void setCreationDateMillis(long creationDateMillis) { this.creationDateMillis = creationDateMillis; }

    public String getKeyAlgorithm() { return keyAlgorithm; }
    public void setKeyAlgorithm(String keyAlgorithm) { this.keyAlgorithm = keyAlgorithm; }

    public String getSha256Fingerprint() { return sha256Fingerprint; }
    public void setSha256Fingerprint(String sha256Fingerprint) { this.sha256Fingerprint = sha256Fingerprint; }

    public String getSha1Fingerprint() { return sha1Fingerprint; }
    public void setSha1Fingerprint(String sha1Fingerprint) { this.sha1Fingerprint = sha1Fingerprint; }

    public String getMd5Fingerprint() { return md5Fingerprint; }
    public void setMd5Fingerprint(String md5Fingerprint) { this.md5Fingerprint = md5Fingerprint; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public int getValidityYears() { return validityYears; }
    public void setValidityYears(int validityYears) { this.validityYears = validityYears; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getKeystoreBase64() { return keystoreBase64; }
    public void setKeystoreBase64(String keystoreBase64) { this.keystoreBase64 = keystoreBase64; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isExpired() {
        long expiryTime = creationDateMillis + ((long) validityYears * 365L * 24L * 60L * 60L * 1000L);
        return System.currentTimeMillis() > expiryTime;
    }

    public int getYearsRemaining() {
        long expiryTime = creationDateMillis + ((long) validityYears * 365L * 24L * 60L * 60L * 1000L);
        long diff = expiryTime - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (int) (diff / (365L * 24L * 60L * 60L * 1000L));
    }
}
