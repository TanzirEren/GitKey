package com.gitkey.build.crypto;

import com.gitkey.build.model.GeneratedSecrets;
import com.gitkey.build.model.KeystoreConfig;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class KeystoreGenerator {

    static {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new BouncyCastleProvider());
    }

    public interface ProgressCallback {
        void onProgress(int stage, String stageMessage);
    }

    public static GeneratedSecrets generate(KeystoreConfig config, ProgressCallback callback) throws Exception {
        if (callback != null) {
            callback.onProgress(1, "Generating signing key pair...");
        }

        // 1. Generate Key Pair
        int keySize = config.getKeySize() > 0 ? config.getKeySize() : 2048;
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keySize, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();

        if (callback != null) {
            callback.onProgress(2, "Creating self-signed X.509 certificate...");
        }

        // 2. Build Distinguished Name (DN)
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        String cn = (config.getCommonName() != null && !config.getCommonName().trim().isEmpty())
                ? config.getCommonName().trim() : (config.getProjectName() + " Developer");
        nameBuilder.addRDN(BCStyle.CN, cn);

        if (config.getOrganizationalUnit() != null && !config.getOrganizationalUnit().trim().isEmpty()) {
            nameBuilder.addRDN(BCStyle.OU, config.getOrganizationalUnit().trim());
        }
        if (config.getOrganization() != null && !config.getOrganization().trim().isEmpty()) {
            nameBuilder.addRDN(BCStyle.O, config.getOrganization().trim());
        }
        if (config.getCity() != null && !config.getCity().trim().isEmpty()) {
            nameBuilder.addRDN(BCStyle.L, config.getCity().trim());
        }
        if (config.getState() != null && !config.getState().trim().isEmpty()) {
            nameBuilder.addRDN(BCStyle.ST, config.getState().trim());
        }
        if (config.getCountry() != null && !config.getCountry().trim().isEmpty()) {
            nameBuilder.addRDN(BCStyle.C, config.getCountry().trim());
        }
        X500Name subjectDN = nameBuilder.build();

        // 3. Create X.509 Certificate
        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - (24L * 60 * 60 * 1000)); // 1 day in the past for clock drift
        int validityYears = config.getValidityYears() > 0 ? config.getValidityYears() : 25;
        Date notAfter = new Date(now + ((long) validityYears * 365L * 24 * 60 * 60 * 1000));
        BigInteger serialNumber = new BigInteger(64, new SecureRandom()).abs();

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subjectDN,
                serialNumber,
                notBefore,
                notAfter,
                subjectDN,
                keyPair.getPublic()
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPair.getPrivate());

        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));

        if (callback != null) {
            callback.onProgress(3, "Packaging into Android signing keystore...");
        }

        // 4. Store in PKCS12 Keystore (Industry standard for modern Android Gradle Plugin & Java)
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        Certificate[] chain = new Certificate[]{cert};
        keyStore.setKeyEntry(
                config.getAlias(),
                keyPair.getPrivate(),
                config.getKeyPassword().toCharArray(),
                chain
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyStore.store(baos, config.getKeystorePassword().toCharArray());
        byte[] keystoreBytes = baos.toByteArray();

        if (callback != null) {
            callback.onProgress(4, "Encoding keystore to Base64...");
        }

        // 5. Encode to Base64
        String base64Keystore = Base64Utils.encode(keystoreBytes);

        if (callback != null) {
            callback.onProgress(5, "Preparing GitHub Actions Secrets...");
        }

        // 6. Compute Certificate Fingerprints
        byte[] certBytes = cert.getEncoded();
        String sha256Fp = HashUtils.sha256Fingerprint(certBytes);
        String sha1Fp = HashUtils.sha1Fingerprint(certBytes);
        String md5Fp = HashUtils.md5Fingerprint(certBytes);

        // 7. Assemble Generated Secrets Container
        GeneratedSecrets secrets = new GeneratedSecrets();
        secrets.setProjectName(config.getProjectName());
        secrets.setKeystoreFileName(config.getKeystoreFileName());
        secrets.setKeystoreBase64(base64Keystore);
        secrets.setKeystorePassword(config.getKeystorePassword());
        secrets.setKeyAlias(config.getAlias());
        secrets.setKeyPassword(config.getKeyPassword());
        secrets.setKeystoreBytes(keystoreBytes);
        secrets.setSha256Fingerprint(sha256Fp);
        secrets.setSha1Fingerprint(sha1Fp);
        secrets.setMd5Fingerprint(md5Fp);

        return secrets;
    }
}
