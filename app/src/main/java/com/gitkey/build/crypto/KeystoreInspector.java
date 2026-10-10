package com.gitkey.build.crypto;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class KeystoreInspector {

    public static class InspectionResult {
        public String keystoreType;
        public List<String> aliases = new ArrayList<>();
        public String selectedAlias;
        public String subjectDN;
        public String issuerDN;
        public String validFrom;
        public String validUntil;
        public String algorithm;
        public String sigAlgorithm;
        public String serialNumber;
        public String sha256Fingerprint;
        public String sha1Fingerprint;
        public String md5Fingerprint;
        public boolean hasPrivateKey;
    }

    public static InspectionResult inspect(InputStream inputStream, String password) throws Exception {
        char[] passChars = password != null ? password.toCharArray() : new char[0];
        KeyStore ks = null;

        // Try PKCS12 first
        byte[] data = new byte[inputStream.available()];
        // Read fully
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] temp = new byte[4096];
        while ((nRead = inputStream.read(temp, 0, temp.length)) != -1) {
            buffer.write(temp, 0, nRead);
        }
        byte[] bytes = buffer.toByteArray();

        String[] types = new String[]{"PKCS12", "BKS", "JKS"};
        for (String type : types) {
            try {
                KeyStore testKs = KeyStore.getInstance(type);
                testKs.load(new java.io.ByteArrayInputStream(bytes), passChars);
                ks = testKs;
                break;
            } catch (Exception ignored) {
            }
        }

        if (ks == null) {
            throw new Exception("Unable to open keystore. Please verify password and format (PKCS12/JKS).");
        }

        InspectionResult result = new InspectionResult();
        result.keystoreType = ks.getType();

        Enumeration<String> enumeration = ks.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            result.aliases.add(alias);
        }

        if (result.aliases.isEmpty()) {
            throw new Exception("Keystore is empty. No keys or certificates found.");
        }

        String primaryAlias = result.aliases.get(0);
        result.selectedAlias = primaryAlias;
        result.hasPrivateKey = ks.isKeyEntry(primaryAlias);

        Certificate cert = ks.getCertificate(primaryAlias);
        if (cert instanceof X509Certificate) {
            X509Certificate x509 = (X509Certificate) cert;
            result.subjectDN = x509.getSubjectDN() != null ? x509.getSubjectDN().getName() : "Unknown";
            result.issuerDN = x509.getIssuerDN() != null ? x509.getIssuerDN().getName() : "Unknown";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
            result.validFrom = sdf.format(x509.getNotBefore());
            result.validUntil = sdf.format(x509.getNotAfter());

            String pubAlg = x509.getPublicKey().getAlgorithm();
            if (x509.getPublicKey() instanceof RSAKey) {
                RSAKey rsa = (RSAKey) x509.getPublicKey();
                result.algorithm = pubAlg + " (" + rsa.getModulus().bitLength() + " bits)";
            } else {
                result.algorithm = pubAlg;
            }

            result.sigAlgorithm = x509.getSigAlgName();
            result.serialNumber = x509.getSerialNumber().toString(16).toUpperCase(Locale.US);

            byte[] certEncoded = x509.getEncoded();
            result.sha256Fingerprint = HashUtils.sha256Fingerprint(certEncoded);
            result.sha1Fingerprint = HashUtils.sha1Fingerprint(certEncoded);
            result.md5Fingerprint = HashUtils.md5Fingerprint(certEncoded);
        }

        return result;
    }
}
