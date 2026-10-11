# GitHub Release v2.0.0

## Release Information
- **Tag**: `v2.0.0`
- **Target Branch**: `main`
- **Release Title**: `GitKey v2.0.0 — Modern Android Keystore Manager & CI/CD Security Toolkit`

---

## 🚀 Release Notes

Welcome to the official **v2.0.0** release of **GitKey**!

This major release features a full visual redesign inspired by the clean, minimal aesthetic of **Google Stitch**, accompanied by **20+ professional developer features** including previous project editing, multi-format file re-downloads, project cloning, search and filtering, and an expanded suite of offline cryptographic utilities.

---

### ✨ What's New in v2.0.0

#### 🎨 Google Stitch Redesign
- **Refined Aesthetics**: Minimal porcelain white light theme and obsidian dark theme with balanced 8dp grid spacing.
- **Glass Dock Navigation**: Modern floating navigation bar with spring-animated micro-interactions.
- **Adaptive Typography & Squircle Cards**: Clean Material 3 typography with rounded cards, subtle 1dp border strokes, and contextual status badges.

#### 🛠️ Comprehensive Project Management
- **View All Details (`ProjectDetailActivity`)**: Full project view with live vault storage status (KB), certificate validity countdown, Distinguished Name (DN) breakdown, and fingerprints.
- **Edit Previous Projects (`EditProjectActivity`)**: Modify project names, key aliases, keystore filenames, validity years, custom project logos, release stage tags (Production, Beta, Staging, Internal), and developer notes.
- **Project Cloning & Templating**: One-tap "Clone Settings" to generate derivative signing keys in seconds.
- **Project Favorites & Pinning (★)**: Star key projects to keep them pinned at the top.
- **Real-Time Search & Category Filters**: Search projects by name, alias, filename, tag, or SHA-256 fingerprint with instant filter chips.

#### 📦 Multi-Format Re-Download & Export Suite
- **Re-Download `.JKS` Files**: Save binary keystore files to external storage using Android Storage Access Framework (SAF).
- **On-the-Fly `.JKS` Reconstruction**: Reconstruct binary keystores from Base64 records if the local file was cleared.
- **Base64 Secrets Export (`.base64.txt`)**: Formatted for GitHub Actions, GitLab CI, Bitrise, CircleCI, and Azure Pipelines.
- **Environment File Export (`.env`)**: Ready-to-use file with all signing environment variables.
- **CI/CD Workflow Export (`build-and-sign.yml`)**: Pre-configured GitHub Actions signing workflow.
- **Public Certificate Export (`.pem`)**: Direct X.509 public certificate extraction for Google Play App Signing (PEP) and Firebase App Check.
- **Vault Backup & Restore (JSON)**: Full database export/import via JSON in Settings for backup and multi-device migration.

#### 🧰 Expanded Developer Utilities (Tools Tab)
- **Keystore Inspector**: Parse external keystores or Base64 strings to check validity, aliases, and SHA fingerprints.
- **Base64 Encoder & Decoder**: Encode and decode text or binary files.
- **Cryptographic Hash Generators**: Calculate SHA-256, SHA-1, and MD5 hashes.
- **Multi-File Checksum Verifier**: Verify file digests and checksum integrity.
- **GitHub Secrets Formatter**: Preview and copy ready-to-paste GitHub Repository Secrets.
- **Cryptographic Password Generator**: Generate high-entropy passwords with rotation micro-animations.
- **UUID Generator**: Cryptographic v4 UUID generator.
- **JSON Formatter & Validator**: Format and validate JSON files.

#### 🛡️ Privacy & Security
- **100% Offline**: Private keys and keystores are generated locally and stored in private app storage.
- **Zero Internet Permissions**: `android.permission.INTERNET` is not included in the manifest.
- **Concealed Secrets**: Passwords and Base64 strings are masked by default with tap-to-reveal toggles.

---

## 📥 Assets to Attach
Attach your compiled GitKey APK below when publishing this release on GitHub:

- `GitKey-v2.0.0.apk` (or `app-release.apk` / `app-debug.apk`)
- Source code (`zip`)
- Source code (`tar.gz`)

---

### 📝 How to Publish on GitHub
1. Go to your repository on GitHub (`https://github.com/your-username/your-repo`).
2. Click on **Releases** ➔ **Draft a new release**.
3. In **Choose a tag**, enter `v2.0.0` and select **Create new tag: v2.0.0 on publish**.
4. In **Release title**, enter:
   ```text
   GitKey v2.0.0 — Modern Android Keystore Manager & CI/CD Security Toolkit
   ```
5. Paste the contents of this file into the release description box.
6. Drag and drop your **GitKey APK** file into the **Attach binaries by dropping them here or selecting them** box.
7. Click **Publish release**.
