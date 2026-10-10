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
    private String logoPath;

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

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
}
