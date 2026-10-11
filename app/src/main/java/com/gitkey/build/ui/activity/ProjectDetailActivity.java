package com.gitkey.build.ui.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.model.ProjectRecord;
import com.gitkey.build.storage.FileExportHelper;
import com.gitkey.build.storage.ProjectRepository;
import com.gitkey.build.utils.ClipboardHelper;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProjectDetailActivity extends AppCompatActivity {

    private ProjectRepository repository;
    private ProjectRecord project;

    private ImageView ivProjectLogo;
    private TextView tvProjectName, tvCreatedDate, tvProjectTag, tvStatusBadge, tvFileName;
    private View dotStatus, layoutVaultStatus;
    private TextView tvVaultStatusText;

    private TextView tvAlias, tvAlgorithm, tvValidity, tvDistinguishedName;
    private TextView tvSha256, tvSha1;
    private TextView tvGradleSnippet, tvNotes;

    private MaterialButton btnDownloadJks, btnExportBase64, btnExportEnv, btnExportYaml, btnExportPem;
    private MaterialButton btnTabKotlinDsl, btnTabGroovy, btnCopyGradle;
    private MaterialButton btnCopyAlias, btnCopySha256, btnCopySha1, btnQuickEditNotes, btnDeleteProject;
    private ImageButton btnBack, btnCloneProject, btnEditProject;

    private boolean isKotlinDslSelected = true;

    // Export types: 1=JKS, 2=Base64, 3=Env, 4=YAML, 5=PEM, 6=Fastlane
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
        setContentView(R.layout.activity_project_detail);

        repository = new ProjectRepository(this);
        String projectId = getIntent().getStringExtra("project_id");
        if (projectId != null) {
            project = repository.getProjectById(projectId);
        }
        if (project == null) {
            project = (ProjectRecord) getIntent().getSerializableExtra("project_record");
        }

        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        bindData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (project != null && project.getId() != null) {
            ProjectRecord updated = repository.getProjectById(project.getId());
            if (updated != null) {
                project = updated;
                bindData();
            }
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCloneProject = findViewById(R.id.btnCloneProject);
        btnEditProject = findViewById(R.id.btnEditProject);

        ivProjectLogo = findViewById(R.id.ivProjectLogo);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvProjectTag = findViewById(R.id.tvProjectTag);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvFileName = findViewById(R.id.tvFileName);
        dotStatus = findViewById(R.id.dotStatus);
        layoutVaultStatus = findViewById(R.id.layoutVaultStatus);
        tvVaultStatusText = findViewById(R.id.tvVaultStatusText);

        btnDownloadJks = findViewById(R.id.btnDownloadJks);
        btnExportBase64 = findViewById(R.id.btnExportBase64);
        btnExportEnv = findViewById(R.id.btnExportEnv);
        btnExportYaml = findViewById(R.id.btnExportYaml);
        btnExportPem = findViewById(R.id.btnExportPem);

        tvAlias = findViewById(R.id.tvAlias);
        btnCopyAlias = findViewById(R.id.btnCopyAlias);
        tvAlgorithm = findViewById(R.id.tvAlgorithm);
        tvValidity = findViewById(R.id.tvValidity);
        tvDistinguishedName = findViewById(R.id.tvDistinguishedName);

        tvSha256 = findViewById(R.id.tvSha256);
        btnCopySha256 = findViewById(R.id.btnCopySha256);
        tvSha1 = findViewById(R.id.tvSha1);
        btnCopySha1 = findViewById(R.id.btnCopySha1);

        btnTabKotlinDsl = findViewById(R.id.btnTabKotlinDsl);
        btnTabGroovy = findViewById(R.id.btnTabGroovy);
        btnCopyGradle = findViewById(R.id.btnCopyGradle);
        tvGradleSnippet = findViewById(R.id.tvGradleSnippet);

        tvNotes = findViewById(R.id.tvNotes);
        btnQuickEditNotes = findViewById(R.id.btnQuickEditNotes);
        btnDeleteProject = findViewById(R.id.btnDeleteProject);
    }

    private void bindData() {
        tvProjectName.setText(project.getProjectName());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault());
        tvCreatedDate.setText(String.format("Created %s", sdf.format(new Date(project.getCreationDateMillis()))));

        String tag = (project.getTag() != null && !project.getTag().isEmpty()) ? project.getTag() : "Release";
        tvProjectTag.setText(tag);

        tvFileName.setText(project.getFileName() != null ? project.getFileName() : project.getProjectName() + "-release.jks");

        // Status
        if (project.isExpired()) {
            tvStatusBadge.setText("Certificate Expired");
            tvStatusBadge.setTextColor(getColor(R.color.brand_rose));
            dotStatus.setBackgroundResource(R.drawable.bg_dot_rose);
        } else {
            int yrs = project.getYearsRemaining();
            tvStatusBadge.setText(String.format("Active • %d yrs left", yrs > 0 ? yrs : 1));
            tvStatusBadge.setTextColor(getColor(R.color.brand_emerald));
            dotStatus.setBackgroundResource(R.drawable.bg_dot_emerald);
        }

        // Vault status check
        File vaultFile = repository.getKeystoreFile(project.getId());
        if (vaultFile != null && vaultFile.exists()) {
            long kb = vaultFile.length() / 1024;
            tvVaultStatusText.setText(String.format(Locale.getDefault(), "Stored locally in GitKey vault (%d KB). Full JKS re-download ready.", kb));
            btnDownloadJks.setEnabled(true);
            btnDownloadJks.setText("Re-Download .JKS Keystore File");
        } else {
            tvVaultStatusText.setText("Vault metadata record. Keystore export available if Base64 copy exists.");
            if (project.getKeystoreBase64() != null && !project.getKeystoreBase64().isEmpty()) {
                btnDownloadJks.setEnabled(true);
                btnDownloadJks.setText("Reconstruct & Download .JKS");
            } else {
                btnDownloadJks.setEnabled(false);
                btnDownloadJks.setText("Keystore File Not In Vault");
            }
        }

        // Logo
        if (project.getLogoPath() != null) {
            try {
                Uri uri = Uri.parse(project.getLogoPath());
                InputStream is = getContentResolver().openInputStream(uri);
                if (is != null) {
                    ivProjectLogo.setImageBitmap(BitmapFactory.decodeStream(is));
                    ivProjectLogo.setPadding(0, 0, 0, 0);
                    is.close();
                } else {
                    ivProjectLogo.setImageResource(R.drawable.ic_gitkey_logo);
                }
            } catch (Exception e) {
                ivProjectLogo.setImageResource(R.drawable.ic_gitkey_logo);
            }
        } else {
            ivProjectLogo.setImageResource(R.drawable.ic_gitkey_logo);
        }

        // Key Details
        tvAlias.setText(project.getAlias() != null ? project.getAlias() : "release_key");
        tvAlgorithm.setText(project.getKeyAlgorithm() != null ? project.getKeyAlgorithm() : "RSA 2048");
        tvValidity.setText(String.format(Locale.getDefault(), "%d Years", project.getValidityYears() > 0 ? project.getValidityYears() : 25));

        StringBuilder dn = new StringBuilder();
        if (project.getCommonName() != null && !project.getCommonName().isEmpty()) {
            dn.append("CN=").append(project.getCommonName()).append(", ");
        }
        if (project.getOrganization() != null && !project.getOrganization().isEmpty()) {
            dn.append("O=").append(project.getOrganization()).append(", ");
        }
        if (project.getCountry() != null && !project.getCountry().isEmpty()) {
            dn.append("C=").append(project.getCountry());
        }
        String dnStr = dn.toString().trim();
        if (dnStr.endsWith(",")) dnStr = dnStr.substring(0, dnStr.length() - 1);
        tvDistinguishedName.setText(!dnStr.isEmpty() ? dnStr : "CN=" + project.getProjectName() + " Developer, O=GitKey, C=US");

        // Fingerprints
        tvSha256.setText(project.getSha256Fingerprint() != null ? project.getSha256Fingerprint() : "N/A");
        tvSha1.setText(project.getSha1Fingerprint() != null ? project.getSha1Fingerprint() : "N/A");

        // Gradle Snippet
        updateGradleSnippet();

        // Notes
        if (project.getNotes() != null && !project.getNotes().trim().isEmpty()) {
            tvNotes.setText(project.getNotes());
        } else {
            tvNotes.setText("No developer notes attached. Tap 'Edit Notes' to document build targets or Play Console release notes.");
        }
    }

    private void updateGradleSnippet() {
        String fileName = project.getFileName() != null ? project.getFileName() : project.getProjectName() + "-release.jks";
        String alias = project.getAlias() != null ? project.getAlias() : "release_key";

        if (isKotlinDslSelected) {
            tvGradleSnippet.setText(FileExportHelper.generateGradleKotlinSnippet(fileName, alias));
            btnTabKotlinDsl.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.brand_blue_tint)));
            btnTabKotlinDsl.setTextColor(getColor(R.color.brand_blue));
            btnTabGroovy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            btnTabGroovy.setTextColor(getColor(R.color.text_secondary_light));
        } else {
            tvGradleSnippet.setText(FileExportHelper.generateGradleGroovySnippet(fileName, alias));
            btnTabGroovy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.brand_blue_tint)));
            btnTabGroovy.setTextColor(getColor(R.color.brand_blue));
            btnTabKotlinDsl.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            btnTabKotlinDsl.setTextColor(getColor(R.color.text_secondary_light));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCloneProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateKeyActivity.class);
            intent.putExtra("clone_project", project);
            startActivity(intent);
        });

        btnEditProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProjectActivity.class);
            intent.putExtra("project_id", project.getId());
            startActivity(intent);
        });

        btnCopyAlias.setOnClickListener(v ->
                ClipboardHelper.copy(this, "Key Alias", project.getAlias(), false));

        btnCopySha256.setOnClickListener(v ->
                ClipboardHelper.copy(this, "SHA-256 Fingerprint", project.getSha256Fingerprint(), false));

        btnCopySha1.setOnClickListener(v ->
                ClipboardHelper.copy(this, "SHA-1 Fingerprint", project.getSha1Fingerprint(), false));

        btnTabKotlinDsl.setOnClickListener(v -> {
            isKotlinDslSelected = true;
            updateGradleSnippet();
        });

        btnTabGroovy.setOnClickListener(v -> {
            isKotlinDslSelected = false;
            updateGradleSnippet();
        });

        btnCopyGradle.setOnClickListener(v ->
                ClipboardHelper.copy(this, "Gradle Signing Config", tvGradleSnippet.getText().toString(), false));

        btnQuickEditNotes.setOnClickListener(v -> showQuickEditNotesDialog());

        // Re-download & Export Suite
        btnDownloadJks.setOnClickListener(v -> {
            pendingExportType = 1;
            String fileName = project.getFileName() != null ? project.getFileName() : project.getProjectName() + "-release.jks";
            exportLauncher.launch(fileName);
        });

        btnExportBase64.setOnClickListener(v -> {
            pendingExportType = 2;
            exportLauncher.launch(project.getProjectName() + "-keystore.base64.txt");
        });

        btnExportEnv.setOnClickListener(v -> {
            pendingExportType = 3;
            exportLauncher.launch(project.getProjectName() + ".env");
        });

        btnExportYaml.setOnClickListener(v -> {
            pendingExportType = 4;
            exportLauncher.launch("build-and-sign.yml");
        });

        btnExportPem.setOnClickListener(v -> {
            pendingExportType = 5;
            exportLauncher.launch(project.getProjectName() + "-cert.pem");
        });

        btnDeleteProject.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Project?")
                    .setMessage("Remove " + project.getProjectName() + " and its vault keystore permanently from your device?")
                    .setPositiveButton("Delete", (d, w) -> {
                        repository.deleteProject(project.getId());
                        Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showQuickEditNotesDialog() {
        EditText input = new EditText(this);
        input.setText(project.getNotes());
        input.setHint("e.g., Used for Google Play Console release v1.0");
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Edit Developer Notes")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newNotes = input.getText().toString().trim();
                    project.setNotes(newNotes);
                    repository.updateProject(project);
                    tvNotes.setText(newNotes.isEmpty() ? "No developer notes attached." : newNotes);
                    Toast.makeText(this, "Notes updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performExportToUri(Uri uri) {
        try {
            switch (pendingExportType) {
                case 1: // JKS
                    byte[] bytes = repository.getKeystoreBytes(project.getId());
                    if (bytes == null && project.getKeystoreBase64() != null) {
                        bytes = android.util.Base64.decode(project.getKeystoreBase64(), android.util.Base64.DEFAULT);
                    }
                    if (bytes != null) {
                        FileExportHelper.writeBytesToUri(this, uri, bytes);
                        Toast.makeText(this, "Keystore exported successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No keystore binary data found", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2: // Base64
                    String b64 = repository.getKeystoreBase64(project.getId());
                    if (b64 != null) {
                        FileExportHelper.writeStringToUri(this, uri, b64);
                        Toast.makeText(this, "Base64 exported successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No Base64 data available", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3: // Env
                    String b64Env = repository.getKeystoreBase64(project.getId());
                    String envContent = FileExportHelper.generateEnvFile(b64Env, project.getAlias());
                    FileExportHelper.writeStringToUri(this, uri, envContent);
                    Toast.makeText(this, ".ENV file exported successfully", Toast.LENGTH_SHORT).show();
                    break;
                case 4: // YAML
                    String yaml = FileExportHelper.generateWorkflowYaml(project.getProjectName(), project.getAlias(), project.getFileName());
                    FileExportHelper.writeStringToUri(this, uri, yaml);
                    Toast.makeText(this, "Workflow YAML exported successfully", Toast.LENGTH_SHORT).show();
                    break;
                case 5: // PEM
                    byte[] jksBytes = repository.getKeystoreBytes(project.getId());
                    String pem = null;
                    if (jksBytes != null) {
                        pem = FileExportHelper.extractCertPemFromKeystoreBytes(jksBytes, "", project.getAlias());
                    }
                    if (pem != null) {
                        FileExportHelper.writeStringToUri(this, uri, pem);
                        Toast.makeText(this, "Public cert (.pem) exported successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not extract PEM certificate", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
