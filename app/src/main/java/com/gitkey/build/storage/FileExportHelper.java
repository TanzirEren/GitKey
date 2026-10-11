package com.gitkey.build.storage;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.gitkey.build.model.GeneratedSecrets;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileExportHelper {

    public static void writeBytesToUri(Context context, Uri uri, byte[] data) throws Exception {
        try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                os.write(data);
                os.flush();
            }
        }
    }

    public static void writeStringToUri(Context context, Uri uri, String text) throws Exception {
        writeBytesToUri(context, uri, text.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateWorkflowYaml(String projectName, String alias, String keystoreFileName) {
        String cleanName = projectName != null ? projectName : "AndroidApp";
        String cleanFile = keystoreFileName != null ? keystoreFileName : "release-key.jks";

        return "name: Build & Sign Android APK\n\n" +
                "on:\n" +
                "  push:\n" +
                "    branches: [ \"main\" ]\n" +
                "  pull_request:\n" +
                "    branches: [ \"main\" ]\n\n" +
                "jobs:\n" +
                "  build:\n" +
                "    runs-on: ubuntu-latest\n\n" +
                "    steps:\n" +
                "    - name: Checkout repository\n" +
                "      uses: actions/checkout@v4\n\n" +
                "    - name: Set up JDK 17\n" +
                "      uses: actions/setup-java@v4\n" +
                "      with:\n" +
                "        java-version: '17'\n" +
                "        distribution: 'temurin'\n" +
                "        cache: gradle\n\n" +
                "    - name: Decode Android Keystore\n" +
                "      run: |\n" +
                "        echo \"${{ secrets.KEYSTORE_BASE64 }}\" | base64 --decode > app/" + cleanFile + "\n\n" +
                "    - name: Grant execute permission for gradlew\n" +
                "      run: chmod +x gradlew\n\n" +
                "    - name: Build with Gradle\n" +
                "      env:\n" +
                "        SIGNING_KEYSTORE_PATH: " + cleanFile + "\n" +
                "        SIGNING_KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}\n" +
                "        SIGNING_KEY_ALIAS: ${{ secrets.KEY_ALIAS }}\n" +
                "        SIGNING_KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}\n" +
                "      run: ./gradlew assembleRelease\n\n" +
                "    - name: Upload APK Artifact\n" +
                "      uses: actions/upload-artifact@v4\n" +
                "      with:\n" +
                "        name: " + cleanName + "-Release\n" +
                "        path: app/build/outputs/apk/release/*.apk\n";
    }

    public static String generateGradleKotlinSnippet(String fileName, String alias) {
        String cleanFile = fileName != null ? fileName : "release-key.jks";
        String cleanAlias = alias != null ? alias : "release_key";
        return "// Android build.gradle.kts (Kotlin DSL)\n" +
                "android {\n" +
                "    signingConfigs {\n" +
                "        create(\"release\") {\n" +
                "            val keystorePath = System.getenv(\"SIGNING_KEYSTORE_PATH\") ?: \"app/" + cleanFile + "\"\n" +
                "            storeFile = file(keystorePath)\n" +
                "            storePassword = System.getenv(\"SIGNING_KEYSTORE_PASSWORD\")\n" +
                "            keyAlias = System.getenv(\"SIGNING_KEY_ALIAS\") ?: \"" + cleanAlias + "\"\n" +
                "            keyPassword = System.getenv(\"SIGNING_KEY_PASSWORD\")\n" +
                "        }\n" +
                "    }\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            signingConfig = signingConfigs.getByName(\"release\")\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    public static String generateGradleGroovySnippet(String fileName, String alias) {
        String cleanFile = fileName != null ? fileName : "release-key.jks";
        String cleanAlias = alias != null ? alias : "release_key";
        return "// Android build.gradle (Groovy DSL)\n" +
                "android {\n" +
                "    signingConfigs {\n" +
                "        release {\n" +
                "            storeFile file(System.getenv(\"SIGNING_KEYSTORE_PATH\") ?: \"" + cleanFile + "\")\n" +
                "            storePassword System.getenv(\"SIGNING_KEYSTORE_PASSWORD\")\n" +
                "            keyAlias System.getenv(\"SIGNING_KEY_ALIAS\") ?: \"" + cleanAlias + "\"\n" +
                "            keyPassword System.getenv(\"SIGNING_KEY_PASSWORD\")\n" +
                "        }\n" +
                "    }\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            signingConfig signingConfigs.release\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    public static String generateGhCliCommands(String base64, String alias) {
        String cleanAlias = alias != null ? alias : "release_key";
        String b64Val = base64 != null ? base64 : "<YOUR_KEYSTORE_BASE64>";
        return "# Run in your Git terminal with GitHub CLI (gh) installed:\n" +
                "gh secret set KEYSTORE_BASE64 -b \"" + b64Val + "\"\n" +
                "gh secret set KEY_ALIAS -b \"" + cleanAlias + "\"\n" +
                "gh secret set KEYSTORE_PASSWORD -b \"<YOUR_KEYSTORE_PASSWORD>\"\n" +
                "gh secret set KEY_PASSWORD -b \"<YOUR_KEY_PASSWORD>\"\n";
    }

    public static String generateFastlaneSnippet(String fileName, String alias) {
        String cleanFile = fileName != null ? fileName : "release-key.jks";
        String cleanAlias = alias != null ? alias : "release_key";
        return "# Fastlane Appfile / Fastfile snippet\n" +
                "lane :build_signed_apk do\n" +
                "  gradle(\n" +
                "    task: 'assembleRelease',\n" +
                "    properties: {\n" +
                "      'android.injected.signing.store.file' => '" + cleanFile + "',\n" +
                "      'android.injected.signing.store.password' => ENV['SIGNING_KEYSTORE_PASSWORD'],\n" +
                "      'android.injected.signing.key.alias' => '" + cleanAlias + "',\n" +
                "      'android.injected.signing.key.password' => ENV['SIGNING_KEY_PASSWORD']\n" +
                "    }\n" +
                "  )\n" +
                "end\n";
    }

    public static String generateEnvFile(String base64, String alias) {
        return "# GitKey Generated Environment Secrets\n" +
                "KEYSTORE_BASE64=" + (base64 != null ? base64 : "") + "\n" +
                "KEY_ALIAS=" + (alias != null ? alias : "release_key") + "\n" +
                "KEYSTORE_PASSWORD=\n" +
                "KEY_PASSWORD=\n";
    }

    public static String extractCertPemFromKeystoreBytes(byte[] jksBytes, String password, String alias) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] pass = (password != null) ? password.toCharArray() : "".toCharArray();
            try {
                ks.load(new ByteArrayInputStream(jksBytes), pass);
            } catch (Exception tryEmpty) {
                // Try null or empty password fallback
                ks.load(new ByteArrayInputStream(jksBytes), null);
            }
            Certificate cert = null;
            if (alias != null && ks.containsAlias(alias)) {
                cert = ks.getCertificate(alias);
            } else if (ks.aliases().hasMoreElements()) {
                cert = ks.getCertificate(ks.aliases().nextElement());
            }
            if (cert != null) {
                return convertCertToPem(cert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertCertToPem(Certificate cert) throws Exception {
        byte[] encoded = cert.getEncoded();
        String base64 = Base64.encodeToString(encoded, Base64.NO_WRAP);
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN CERTIFICATE-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            int end = Math.min(i + 64, base64.length());
            sb.append(base64, i, end).append("\n");
        }
        sb.append("-----END CERTIFICATE-----\n");
        return sb.toString();
    }

    public static String generateProjectSummary(GeneratedSecrets secrets) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
        return "====================================================\n" +
                "              GITKEY PROJECT SIGNING SUMMARY        \n" +
                "====================================================\n\n" +
                "Project Name:     " + secrets.getProjectName() + "\n" +
                "Creation Date:    " + sdf.format(new Date(secrets.getCreationTimestamp())) + "\n" +
                "Keystore File:    " + secrets.getKeystoreFileName() + "\n" +
                "Key Alias:        " + secrets.getKeyAlias() + "\n\n" +
                "----------------- CERTIFICATE FINGERPRINTS ---------\n" +
                "SHA-256:          " + secrets.getSha256Fingerprint() + "\n" +
                "SHA-1:            " + secrets.getSha1Fingerprint() + "\n" +
                "MD5:              " + secrets.getMd5Fingerprint() + "\n\n" +
                "----------------- GITHUB ACTIONS SECRETS -----------\n" +
                "Add these 4 secrets to your GitHub repository under:\n" +
                "Settings -> Secrets and variables -> Actions\n\n" +
                "1. KEYSTORE_BASE64\n" +
                "2. KEYSTORE_PASSWORD\n" +
                "3. KEY_ALIAS\n" +
                "4. KEY_PASSWORD\n\n" +
                "----------------- SECURITY REMINDER ----------------\n" +
                "Never commit your .jks keystore or plain passwords into\n" +
                "public Git repositories. Keep your backup safe!\n" +
                "Generated by GitKey - Generate once. Sign anywhere.\n";
    }
}
