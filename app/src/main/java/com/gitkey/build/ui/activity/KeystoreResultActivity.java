package com.gitkey.build.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.model.GeneratedSecrets;
import com.gitkey.build.storage.FileExportHelper;
import com.gitkey.build.utils.ClipboardHelper;
import com.gitkey.build.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class KeystoreResultActivity extends AppCompatActivity {

    private GeneratedSecrets secrets;
    private PreferencesManager prefs;

    private TextView tvResultProjectName, tvResultFileName;
    private TextView tvSecretBase64, tvSecretKeystorePassword, tvSecretKeyAlias, tvSecretKeyPassword;
    private TextView tvFingerprintSha256, tvFingerprintSha1;

    private MaterialButton btnToggleBase64, btnToggleKeystorePassword, btnToggleKeyPassword;
    private MaterialButton btnCopyBase64, btnCopyKeystorePassword, btnCopyKeyAlias, btnCopyKeyPassword;
    private MaterialButton btnCopyAll, btnExportFiles, btnGithubGuide;
    private ImageButton btnDone;

    private boolean isBase64Visible = false;
    private boolean isKeystorePasswordVisible = false;
    private boolean isKeyPasswordVisible = false;

    // Export type trackers: 1 = JKS, 2 = Secrets, 3 = Workflow, 4 = Summary
    private int pendingExportType = 0;

    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("*/*"),
            uri -> {
                if (uri != null) {
                    performExportToUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keystore_result);

        prefs = new PreferencesManager(this);
        secrets = (GeneratedSecrets) getIntent().getSerializableExtra("generated_secrets");

        if (secrets == null) {
            Toast.makeText(this, "No secrets found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        btnDone = findViewById(R.id.btnDone);
        tvResultProjectName = findViewById(R.id.tvResultProjectName);
        tvResultFileName = findViewById(R.id.tvResultFileName);

        tvSecretBase64 = findViewById(R.id.tvSecretBase64);
        tvSecretKeystorePassword = findViewById(R.id.tvSecretKeystorePassword);
        tvSecretKeyAlias = findViewById(R.id.tvSecretKeyAlias);
        tvSecretKeyPassword = findViewById(R.id.tvSecretKeyPassword);

        tvFingerprintSha256 = findViewById(R.id.tvFingerprintSha256);
        tvFingerprintSha1 = findViewById(R.id.tvFingerprintSha1);

        btnToggleBase64 = findViewById(R.id.btnToggleBase64);
        btnToggleKeystorePassword = findViewById(R.id.btnToggleKeystorePassword);
        btnToggleKeyPassword = findViewById(R.id.btnToggleKeyPassword);

        btnCopyBase64 = findViewById(R.id.btnCopyBase64);
        btnCopyKeystorePassword = findViewById(R.id.btnCopyKeystorePassword);
        btnCopyKeyAlias = findViewById(R.id.btnCopyKeyAlias);
        btnCopyKeyPassword = findViewById(R.id.btnCopyKeyPassword);

        MaterialButton btnCopySha256 = findViewById(R.id.btnCopySha256);
        MaterialButton btnCopySha1 = findViewById(R.id.btnCopySha1);

        btnCopyAll = findViewById(R.id.btnCopyAll);
        btnExportFiles = findViewById(R.id.btnExportFiles);
        btnGithubGuide = findViewById(R.id.btnGithubGuide);
    }

    private void bindData() {
        tvResultProjectName.setText(secrets.getProjectName());
        tvResultFileName.setText(secrets.getKeystoreFileName());
        tvSecretKeyAlias.setText(secrets.getKeyAlias());
        tvFingerprintSha256.setText(secrets.getSha256Fingerprint());
        tvFingerprintSha1.setText(secrets.getSha1Fingerprint());

        if (!prefs.isHideSecretsByDefault()) {
            isBase64Visible = true;
            isKeystorePasswordVisible = true;
            isKeyPasswordVisible = true;
        }

        updateVisibilityUi();
    }

    private void updateVisibilityUi() {
        if (isBase64Visible) {
            tvSecretBase64.setText(secrets.getKeystoreBase64());
            btnToggleBase64.setText(R.string.btn_hide);
        } else {
            tvSecretBase64.setText("••••••••••••••••••••••••••••••••••••••••••••••••••••");
            btnToggleBase64.setText(R.string.btn_show);
        }

        if (isKeystorePasswordVisible) {
            tvSecretKeystorePassword.setText(secrets.getKeystorePassword());
            btnToggleKeystorePassword.setText(R.string.btn_hide);
        } else {
            tvSecretKeystorePassword.setText("••••••••••••••••");
            btnToggleKeystorePassword.setText(R.string.btn_show);
        }

        if (isKeyPasswordVisible) {
            tvSecretKeyPassword.setText(secrets.getKeyPassword());
            btnToggleKeyPassword.setText(R.string.btn_hide);
        } else {
            tvSecretKeyPassword.setText("••••••••••••••••");
            btnToggleKeyPassword.setText(R.string.btn_show);
        }
    }

    private void setupListeners() {
        btnDone.setOnClickListener(v -> finish());

        btnToggleBase64.setOnClickListener(v -> {
            isBase64Visible = !isBase64Visible;
            updateVisibilityUi();
        });

        btnToggleKeystorePassword.setOnClickListener(v -> {
            isKeystorePasswordVisible = !isKeystorePasswordVisible;
            updateVisibilityUi();
        });

        btnToggleKeyPassword.setOnClickListener(v -> {
            isKeyPasswordVisible = !isKeyPasswordVisible;
            updateVisibilityUi();
        });

        btnCopyBase64.setOnClickListener(v ->
                ClipboardHelper.copy(this, "KEYSTORE_BASE64", secrets.getKeystoreBase64(), true));

        btnCopyKeystorePassword.setOnClickListener(v ->
                ClipboardHelper.copy(this, "KEYSTORE_PASSWORD", secrets.getKeystorePassword(), true));

        btnCopyKeyAlias.setOnClickListener(v ->
                ClipboardHelper.copy(this, "KEY_ALIAS", secrets.getKeyAlias(), false));

        btnCopyKeyPassword.setOnClickListener(v ->
                ClipboardHelper.copy(this, "KEY_PASSWORD", secrets.getKeyPassword(), true));

        MaterialButton btnCopySha256 = findViewById(R.id.btnCopySha256);
        if (btnCopySha256 != null) {
            btnCopySha256.setOnClickListener(v ->
                    ClipboardHelper.copy(this, "SHA-256 Fingerprint", secrets.getSha256Fingerprint(), false));
        }
        tvFingerprintSha256.setOnClickListener(v ->
                ClipboardHelper.copy(this, "SHA-256 Fingerprint", secrets.getSha256Fingerprint(), false));

        MaterialButton btnCopySha1 = findViewById(R.id.btnCopySha1);
        if (btnCopySha1 != null) {
            btnCopySha1.setOnClickListener(v ->
                    ClipboardHelper.copy(this, "SHA-1 Fingerprint", secrets.getSha1Fingerprint(), false));
        }
        tvFingerprintSha1.setOnClickListener(v ->
                ClipboardHelper.copy(this, "SHA-1 Fingerprint", secrets.getSha1Fingerprint(), false));

        btnCopyAll.setOnClickListener(v -> {
            ClipboardHelper.copy(this, "All GitHub Secrets", secrets.getFormattedAllSecrets(), true);
            Snackbar.make(findViewById(android.R.id.content),
                    "GitHub Secrets copied to clipboard", Snackbar.LENGTH_LONG)
                    .setAction("Setup Guide", view -> {
                        startActivity(new Intent(KeystoreResultActivity.this, GitHubActionsGuideActivity.class));
                    }).show();
        });

        btnExportFiles.setOnClickListener(v -> showExportDialog());

        btnGithubGuide.setOnClickListener(v -> {
            Intent intent = new Intent(KeystoreResultActivity.this, GitHubActionsGuideActivity.class);
            startActivity(intent);
        });
    }

    private void showExportDialog() {
        if (prefs.isRequireExportConfirmation()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.export_dialog_title)
                    .setMessage(R.string.export_dialog_warning)
                    .setPositiveButton("I Understand, Continue", (d, w) -> showExportOptionsDialog())
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        } else {
            showExportOptionsDialog();
        }
    }

    private void showExportOptionsDialog() {
        String[] options = new String[]{
                "1. Save Keystore (.jks)",
                "2. Save Secrets File (.env / .txt)",
                "3. Save GitHub Actions Workflow (.yml)",
                "4. Save Project Summary (.txt)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select File to Export")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            pendingExportType = 1;
                            exportLauncher.launch(secrets.getKeystoreFileName());
                            break;
                        case 1:
                            pendingExportType = 2;
                            exportLauncher.launch(secrets.getProjectName() + "-secrets.env");
                            break;
                        case 2:
                            pendingExportType = 3;
                            exportLauncher.launch("android-release.yml");
                            break;
                        case 3:
                            pendingExportType = 4;
                            exportLauncher.launch(secrets.getProjectName() + "-summary.txt");
                            break;
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void performExportToUri(Uri uri) {
        try {
            switch (pendingExportType) {
                case 1:
                    FileExportHelper.writeBytesToUri(this, uri, secrets.getKeystoreBytes());
                    Toast.makeText(this, "Keystore (.jks) exported successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    FileExportHelper.writeStringToUri(this, uri, secrets.getFormattedAllSecrets());
                    Toast.makeText(this, "Secrets (.env) exported successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    String yml = FileExportHelper.generateWorkflowYaml(
                            secrets.getProjectName(),
                            secrets.getKeyAlias(),
                            secrets.getKeystoreFileName());
                    FileExportHelper.writeStringToUri(this, uri, yml);
                    Toast.makeText(this, "Workflow (.yml) exported successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    String summary = FileExportHelper.generateProjectSummary(secrets);
                    FileExportHelper.writeStringToUri(this, uri, summary);
                    Toast.makeText(this, "Project summary exported successfully!", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
