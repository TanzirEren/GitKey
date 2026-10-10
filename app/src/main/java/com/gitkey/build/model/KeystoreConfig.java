package com.gitkey.build.model;

import android.net.Uri;

public class KeystoreConfig {
    private String projectName;
    private String keystoreFileName;
    private String alias;
    private String keystorePassword;
    private String keyPassword;
    private String algorithm = "RSA";
    private int keySize = 2048;
    private int validityYears = 25;
    private String commonName = "Android Developer";
    private String organization = "GitKey Development";
    private String organizationalUnit = "Mobile";
    private String city = "San Francisco";
    private String state = "CA";
    private String country = "US";
    private Uri logoUri;

    public KeystoreConfig() {}

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getKeystoreFileName() { return keystoreFileName; }
    public void setKeystoreFileName(String keystoreFileName) { this.keystoreFileName = keystoreFileName; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

    public String getKeyPassword() { return keyPassword; }
    public void setKeyPassword(String keyPassword) { this.keyPassword = keyPassword; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public int getKeySize() { return keySize; }
    public void setKeySize(int keySize) { this.keySize = keySize; }

    public int getValidityYears() { return validityYears; }
    public void setValidityYears(int validityYears) { this.validityYears = validityYears; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getOrganizationalUnit() { return organizationalUnit; }
    public void setOrganizationalUnit(String organizationalUnit) { this.organizationalUnit = organizationalUnit; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Uri getLogoUri() { return logoUri; }
    public void setLogoUri(Uri logoUri) { this.logoUri = logoUri; }
}
