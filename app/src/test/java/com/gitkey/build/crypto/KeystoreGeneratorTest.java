package com.gitkey.build.crypto;

import com.gitkey.build.model.GeneratedSecrets;
import com.gitkey.build.model.KeystoreConfig;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class KeystoreGeneratorTest {

    @Test
    public void testSecurePasswordGenerator() {
        String pass1 = SecurePasswordGenerator.generatePassword(16);
        String pass2 = SecurePasswordGenerator.generatePassword(16);

        Assert.assertEquals(16, pass1.length());
        Assert.assertEquals(16, pass2.length());
        Assert.assertNotEquals(pass1, pass2);
    }

    @Test
    public void testHashUtils() {
        byte[] input = "GitKey-Test-Signing".getBytes(StandardCharsets.UTF_8);

        String sha256 = HashUtils.sha256(input);
        Assert.assertNotNull(sha256);
        Assert.assertEquals(64, sha256.length());

        String sha1 = HashUtils.sha1(input);
        Assert.assertNotNull(sha1);
        Assert.assertEquals(40, sha1.length());

        String md5 = HashUtils.md5(input);
        Assert.assertNotNull(md5);
        Assert.assertEquals(32, md5.length());
    }

    @Test
    public void testKeystoreGenerationAndInspection() throws Exception {
        KeystoreConfig config = new KeystoreConfig();
        config.setProjectName("TestApp");
        config.setKeystoreFileName("TestApp-release.jks");
        config.setAlias("test_key");
        config.setKeystorePassword("storePassword123!");
        config.setKeyPassword("keyPassword123!");
        config.setKeySize(2048);
        config.setValidityYears(25);
        config.setCommonName("Test Developer");
        config.setOrganization("Test Org");

        GeneratedSecrets secrets = KeystoreGenerator.generate(config, null);

        Assert.assertNotNull(secrets);
        Assert.assertEquals("TestApp", secrets.getProjectName());
        Assert.assertEquals("test_key", secrets.getKeyAlias());
        Assert.assertEquals("storePassword123!", secrets.getKeystorePassword());
        Assert.assertEquals("keyPassword123!", secrets.getKeyPassword());
        Assert.assertNotNull(secrets.getKeystoreBase64());
        Assert.assertTrue(secrets.getKeystoreBase64().length() > 500);
        Assert.assertNotNull(secrets.getSha256Fingerprint());
        Assert.assertNotNull(secrets.getSha1Fingerprint());

        // Test Inspection
        ByteArrayInputStream bais = new ByteArrayInputStream(secrets.getKeystoreBytes());
        KeystoreInspector.InspectionResult inspection = KeystoreInspector.inspect(bais, "storePassword123!");

        Assert.assertNotNull(inspection);
        Assert.assertEquals("test_key", inspection.selectedAlias);
        Assert.assertEquals(secrets.getSha256Fingerprint(), inspection.sha256Fingerprint);
        Assert.assertEquals(secrets.getSha1Fingerprint(), inspection.sha1Fingerprint);
    }
}
