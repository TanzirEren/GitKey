# GitKey 🗝️

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Nougat)-blue.svg)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green.svg)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java%2011-ED8B00.svg?style=flat&logo=openjdk)](https://www.java.com/)
[![Design](https://img.shields.io/badge/Design-Google%20Stitch%20%2F%20M3-2563EB.svg)](https://m3.material.io/)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20%26%20Zero--Telemetry-10B981.svg)](#security--privacy)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

> **Generate once. Sign anywhere.**  
> GitKey is an offline Android keystore generator, signing key manager, and GitHub Actions CI/CD secrets toolkit designed with Google Stitch minimalism.

---

## 📖 Overview

Generating Android signing keystores traditionally requires terminal commands (`keytool`), complicated Distinguished Name (DN) parameters, and manual Base64 conversions for continuous integration (CI/CD) pipelines.

**GitKey** brings production-grade cryptographic key management directly to Android. Build industry-standard `.jks` keystores, inspect certificates, re-download binary or environment configurations, edit previous projects, and format GitHub Repository Secrets in seconds—all completely offline without internet permissions or cloud tracking.

---

## ⚡ What's New in GitKey v2

GitKey v2 introduces a complete **Google Stitch** visual redesign (minimal porcelain light theme, obsidian slate dark theme, refined typography, micro-animations, and floating glass dock navigation) alongside **20+ professional developer features**:

1. **Comprehensive Project Details Hub** (`ProjectDetailActivity`)  
   View complete details of any saved project: Key Alias, Keystore Filename, Creation Date, Certificate Validity, and Expiration countdown.
2. **Edit Previous Projects** (`EditProjectActivity`)  
   Update Project Name, Key Alias, Keystore Filename, Release Tags, Validity, Certificate Identity (CN, Organization, Country), Project Avatars, and Developer Notes.
3. **Multi-Format Re-Download Suite**  
   Re-download raw binary `.jks` files directly to device storage via Android Storage Access Framework (SAF). Reconstruct `.jks` from Base64 on demand.
4. **CI/CD Secret Exports**  
   Export `.base64.txt` files formatted for GitHub Actions, GitLab CI, Bitrise, CircleCI, or Azure Pipelines.
5. **Ready-to-Use `.env` Configurations**  
   Export `.env` files containing `ANDROID_KEYSTORE_BASE64`, `KEY_ALIAS`, `KEYSTORE_PASSWORD`, and `KEY_PASSWORD`.
6. **Automated GitHub Actions Workflows**  
   Export pre-configured `.github/workflows/build-and-sign.yml` files for Gradle assemble/bundle tasks.
7. **Public Certificate Export (`.pem`)**  
   Extract and download public X.509 `.pem` certificates directly from stored keystores for Google Play App Signing (PEP) or Firebase App Check.
8. **Keystore Cloning & Templating**  
   One-tap "Clone Settings" pre-fills key creation parameters from an existing project while generating fresh credentials.
9. **Project Favorites & Pinning (★)**  
   Star important production keys to keep them pinned at the top of your workspace.
10. **Real-Time Project Search & Filtering**  
    Instant search by Project Name, Alias, Filename, Tag, or SHA-256 fingerprint, paired with filter chips (**All**, **★ Starred**, **Active**, **Expired**).
11. **Interactive Gradle Build Integration**  
    Toggle between **Kotlin DSL (`.kts`)** and **Groovy (`.gradle`)** `signingConfigs` blocks with one-tap copy buttons.
12. **Direct Fingerprint Copying**  
    Copy SHA-256, SHA-1, and MD5 fingerprints formatted for Google Play Console and Firebase.
13. **Certificate Expiration & Health Monitor**  
    Live countdown and status indicators (*Active • X yrs left* or *Certificate Expired*).
14. **Quick Developer Notes Editor**  
    Quickly update release notes, target Play Console tracks, or deployment instructions from project details.
15. **Full Project Vault Backup (JSON)**  
    Export your complete database and vault metadata to a portable `.json` backup file.
16. **Full Project Vault Restore (JSON)**  
    Import and restore projects from backup files across devices or fresh installs.
17. **Keystore Inspector**  
    Inspect external `.jks` or `.keystore` files or Base64 strings to view certificate validity, aliases, and SHA fingerprints.
18. **Base64 Encoder & Decoder**  
    Encode text or arbitrary binary files into Base64 and decode Base64 strings back to text or files.
19. **Cryptographic Hash Generators**  
    Calculate SHA-256, SHA-1, and MD5 hashes from text or files.
20. **Multi-File Checksum Verifier**  
    Verify file integrity against expected checksum digests.
21. **GitHub Secrets Formatter**  
    Format and preview repository secrets for GitHub Actions with single-click copy buttons.
22. **Cryptographic Password Generator**  
    Configurable high-entropy password generator with smooth 360-degree rotation animation.
23. **UUID / GUID Generator**  
    Generate v4 UUIDs for build IDs, release tags, and telemetry tokens.
24. **JSON Formatter & Validator**  
    Validate, format, and minify JSON payloads.
25. **Interactive CI/CD Guide**  
    Step-by-step tutorial on configuring GitHub Repository Secrets and Play Console App Signing.
26. **Google Stitch Adaptive Themes**  
    Switch between Porcelain Light mode, Obsidian Dark mode, or System Default with spring-animated dock tabs.

---

## 🔒 Security & Privacy

- **100% Offline Generation**: Keystores and cryptographic keys are generated entirely on-device using Android's native `java.security.KeyStore`, `java.security.KeyPairGenerator`, and standard JCA providers.
- **Zero Internet Permissions**: GitKey does **NOT** request the `android.permission.INTERNET` permission in its manifest. Your private keys never leave your physical device.
- **Local Sandbox Storage**: Saved keystores are stored in the application's private internal storage (`/data/user/0/com.gitkey.build/files/vault_keystores/`), inaccessible to other apps.
- **Concealed Secrets by Default**: Passwords and Base64 strings are masked by default with tap-to-reveal toggles.
- **Export Confirmation**: Optional confirmation guards prevent accidental export of sensitive signing materials.

---

## 🛠️ GitHub Actions CI/CD Integration

To sign your Android release APK / AAB automatically using GitHub Actions:

### 1. Add Secrets to GitHub
Go to **Your Repository** ➔ **Settings** ➔ **Secrets and variables** ➔ **Actions** ➔ **New repository secret**:

| Secret Name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Paste the Base64 string exported from GitKey |
| `KEY_ALIAS` | Your Key Alias (e.g., `release_key`) |
| `KEYSTORE_PASSWORD` | Your Keystore Password |
| `KEY_PASSWORD` | Your Key Password |

### 2. Workflow Example (`.github/workflows/build-and-sign.yml`)

```yaml
name: Build and Sign Android Release

on:
  push:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Decode Android Keystore
        run: |
          echo "${{ secrets.ANDROID_KEYSTORE_BASE64 }}" | base64 --decode > app/release-keystore.jks

      - name: Build Android App Bundle (.aab)
        env:
          KEYSTORE_PATH: "release-keystore.jks"
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-release
          path: app/build/outputs/bundle/release/*.aab
```

---

## 💻 Tech Stack & Architecture

- **Language**: Java 11
- **Architecture**: Modular MVVM-inspired design with Storage Access Framework (SAF)
- **UI Framework**: Android Material Components 3 (M3) + Custom Google Stitch styling
- **Cryptography**: Standard Java Cryptography Architecture (`java.security`, `javax.crypto`, `KeyStore`, `X509Certificate`)
- **Persistence**: SharedPreferences with structured JSON vault indexing and file sandboxing

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or IntelliJ IDEA with Android plugin
- Android SDK 36 (compileSdk)
- JDK 11 or higher
- Gradle 8.11+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/your-username/gitkey.git
cd gitkey

# Build Debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.
