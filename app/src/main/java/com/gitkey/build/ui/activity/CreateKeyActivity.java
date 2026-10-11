package com.gitkey.build.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.crypto.KeystoreGenerator;
import com.gitkey.build.crypto.SecurePasswordGenerator;
import com.gitkey.build.model.GeneratedSecrets;
import com.gitkey.build.model.KeystoreConfig;
import com.gitkey.build.model.ProjectRecord;
import com.gitkey.build.storage.ProjectRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateKeyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputLayout tilProjectName, tilKeyAlias, tilKeystorePassword, tilKeyPassword;
    private TextInputLayout tilKeySize, tilValidity, tilCommonName, tilOrganization, tilOrgUnit, tilCity, tilState, tilCountry;
    private TextInputEditText etProjectName, etKeyAlias, etKeystorePassword, etKeyPassword;
    private TextInputEditText etKeySize, etValidity, etCommonName, etOrganization, etOrgUnit, etCity, etState, etCountry;
    private TextView tvKeystoreFileName;
    private ImageView ivLogoPreview, ivAdvancedArrow;
    private MaterialButton btnSelectLogo, btnGenKeystorePassword, btnGenKeyPassword, btnBuildKeystore;
    private MaterialCheckBox cbSamePassword;
    private LinearLayout layoutKeyPasswordContainer, btnToggleAdvanced, layoutAdvancedContent;
    private FrameLayoutOverlay progressOverlay;

    private Uri selectedLogoUri = null;
    private boolean isAdvancedExpanded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> logoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedLogoUri = uri;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        if (is != null) {
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            ivLogoPreview.setImageBitmap(bitmap);
                            ivLogoPreview.setImageTintList(null);
                            is.close();
                            btnSelectLogo.setText(R.string.btn_change_logo);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_key);

        initViews();
        setupListeners();
        setDefaultPasswords();
        checkForCloneIntent();
    }

    private void checkForCloneIntent() {
        if (getIntent().hasExtra("clone_project")) {
            ProjectRecord clone = (ProjectRecord) getIntent().getSerializableExtra("clone_project");
            if (clone != null) {
                etProjectName.setText(clone.getProjectName() + " Copy");
                etKeyAlias.setText(clone.getAlias() != null ? clone.getAlias() + "_v2" : "release_key");
                if (clone.getCommonName() != null && !clone.getCommonName().isEmpty()) {
                    etCommonName.setText(clone.getCommonName());
                }
                if (clone.getOrganization() != null && !clone.getOrganization().isEmpty()) {
                    etOrganization.setText(clone.getOrganization());
                }
                if (clone.getCountry() != null && !clone.getCountry().isEmpty()) {
                    etCountry.setText(clone.getCountry());
                }
                if (clone.getValidityYears() > 0) {
                    etValidity.setText(String.valueOf(clone.getValidityYears()));
                }
            }
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tilProjectName = findViewById(R.id.tilProjectName);
        etProjectName = findViewById(R.id.etProjectName);
        tvKeystoreFileName = findViewById(R.id.tvKeystoreFileName);

        ivLogoPreview = findViewById(R.id.ivLogoPreview);
        btnSelectLogo = findViewById(R.id.btnSelectLogo);

        tilKeyAlias = findViewById(R.id.tilKeyAlias);
        etKeyAlias = findViewById(R.id.etKeyAlias);

        tilKeystorePassword = findViewById(R.id.tilKeystorePassword);
        etKeystorePassword = findViewById(R.id.etKeystorePassword);
        btnGenKeystorePassword = findViewById(R.id.btnGenKeystorePassword);

        cbSamePassword = findViewById(R.id.cbSamePassword);
        layoutKeyPasswordContainer = findViewById(R.id.layoutKeyPasswordContainer);

        tilKeyPassword = findViewById(R.id.tilKeyPassword);
        etKeyPassword = findViewById(R.id.etKeyPassword);
        btnGenKeyPassword = findViewById(R.id.btnGenKeyPassword);

        btnToggleAdvanced = findViewById(R.id.btnToggleAdvanced);
        ivAdvancedArrow = findViewById(R.id.ivAdvancedArrow);
        layoutAdvancedContent = findViewById(R.id.layoutAdvancedContent);

        tilKeySize = findViewById(R.id.tilKeySize);
        etKeySize = findViewById(R.id.etKeySize);
        tilValidity = findViewById(R.id.tilValidity);
        etValidity = findViewById(R.id.etValidity);
        tilCommonName = findViewById(R.id.tilCommonName);
        etCommonName = findViewById(R.id.etCommonName);
        tilOrganization = findViewById(R.id.tilOrganization);
        etOrganization = findViewById(R.id.etOrganization);
        tilOrgUnit = findViewById(R.id.tilOrgUnit);
        etOrgUnit = findViewById(R.id.etOrgUnit);
        tilCity = findViewById(R.id.tilCity);
        etCity = findViewById(R.id.etCity);
        tilState = findViewById(R.id.tilState);
        etState = findViewById(R.id.etState);
        tilCountry = findViewById(R.id.tilCountry);
        etCountry = findViewById(R.id.etCountry);

        btnBuildKeystore = findViewById(R.id.btnBuildKeystore);
        progressOverlay = new FrameLayoutOverlay(findViewById(R.id.overlayProgress),
                findViewById(R.id.tvProgressStage),
                findViewById(R.id.tvProgressDetail));
    }

    private void setDefaultPasswords() {
        String defaultPass = SecurePasswordGenerator.generateDefault();
        etKeystorePassword.setText(defaultPass);
        etKeyPassword.setText(defaultPass);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etProjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                String name = s.toString().trim();
                if (!name.isEmpty()) {
                    tilProjectName.setError(null);
                    String sanitized = name.replaceAll("[^a-zA-Z0-9_-]", "");
                    tvKeystoreFileName.setText("Default file: " + sanitized + "-release.jks");
                    if (etKeyAlias.getText() == null || etKeyAlias.getText().toString().trim().isEmpty() ||
                            etKeyAlias.getText().toString().equals("release_key") ||
                            etKeyAlias.getText().toString().startsWith(sanitized.toLowerCase())) {
                        etKeyAlias.setText(sanitized.toLowerCase() + "_key");
                    }
                    if (etCommonName.getText() == null || etCommonName.getText().toString().trim().isEmpty() ||
                            etCommonName.getText().toString().endsWith("Developer")) {
                        etCommonName.setText(name + " Developer");
                    }
                } else {
                    tvKeystoreFileName.setText("Default file: MyApp-release.jks");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSelectLogo.setOnClickListener(v -> logoPickerLauncher.launch("image/*"));

        btnGenKeystorePassword.setOnClickListener(v -> {
            v.animate().rotationBy(360f).setDuration(350).start();
            String pass = SecurePasswordGenerator.generateDefault();
            etKeystorePassword.setText(pass);
            if (cbSamePassword.isChecked()) {
                etKeyPassword.setText(pass);
            }
            Toast.makeText(this, "Generated secure keystore password", Toast.LENGTH_SHORT).show();
        });

        btnGenKeyPassword.setOnClickListener(v -> {
            v.animate().rotationBy(360f).setDuration(350).start();
            String pass = SecurePasswordGenerator.generateDefault();
            etKeyPassword.setText(pass);
            Toast.makeText(this, "Generated secure key password", Toast.LENGTH_SHORT).show();
        });

        cbSamePassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutKeyPasswordContainer.setVisibility(View.GONE);
                etKeyPassword.setText(etKeystorePassword.getText());
            } else {
                layoutKeyPasswordContainer.setVisibility(View.VISIBLE);
            }
        });

        btnToggleAdvanced.setOnClickListener(v -> {
            isAdvancedExpanded = !isAdvancedExpanded;
            ivAdvancedArrow.animate().rotation(isAdvancedExpanded ? 180f : 0f).setDuration(250).start();
            if (isAdvancedExpanded) {
                layoutAdvancedContent.setVisibility(View.VISIBLE);
            } else {
                layoutAdvancedContent.setVisibility(View.GONE);
            }
        });

        btnBuildKeystore.setOnClickListener(v -> startKeystoreBuild());
    }

    private void startKeystoreBuild() {
        String projectName = etProjectName.getText() != null ? etProjectName.getText().toString().trim() : "";
        if (projectName.isEmpty()) {
            tilProjectName.setError(getString(R.string.error_project_name_empty));
            etProjectName.requestFocus();
            return;
        }

        String alias = etKeyAlias.getText() != null ? etKeyAlias.getText().toString().trim() : "";
        if (alias.isEmpty()) {
            tilKeyAlias.setError(getString(R.string.error_alias_empty));
            etKeyAlias.requestFocus();
            return;
        }

        String keystorePass = etKeystorePassword.getText() != null ? etKeystorePassword.getText().toString() : "";
        if (keystorePass.length() < 6) {
            tilKeystorePassword.setError(getString(R.string.error_keystore_password_short));
            etKeystorePassword.requestFocus();
            return;
        }

        String keyPass = cbSamePassword.isChecked() ? keystorePass :
                (etKeyPassword.getText() != null ? etKeyPassword.getText().toString() : "");
        if (keyPass.length() < 6) {
            tilKeyPassword.setError(getString(R.string.error_key_password_short));
            etKeyPassword.requestFocus();
            return;
        }

        int validity = 25;
        try {
            if (etValidity.getText() != null && !etValidity.getText().toString().trim().isEmpty()) {
                validity = Integer.parseInt(etValidity.getText().toString().trim());
                if (validity < 1 || validity > 100) {
                    tilValidity.setError(getString(R.string.error_validity_invalid));
                    return;
                }
            }
        } catch (Exception e) {
            tilValidity.setError(getString(R.string.error_validity_invalid));
            return;
        }

        int keySize = 2048;
        try {
            if (etKeySize.getText() != null && !etKeySize.getText().toString().trim().isEmpty()) {
                keySize = Integer.parseInt(etKeySize.getText().toString().trim());
            }
        } catch (Exception ignored) {}

        // Clear errors
        tilProjectName.setError(null);
        tilKeyAlias.setError(null);
        tilKeystorePassword.setError(null);
        tilKeyPassword.setError(null);
        tilValidity.setError(null);

        // Build Config
        String sanitizedName = projectName.replaceAll("[^a-zA-Z0-9_-]", "");
        String fileName = sanitizedName + "-release.jks";

        KeystoreConfig config = new KeystoreConfig();
        config.setProjectName(projectName);
        config.setKeystoreFileName(fileName);
        config.setAlias(alias);
        config.setKeystorePassword(keystorePass);
        config.setKeyPassword(keyPass);
        config.setKeySize(keySize);
        config.setValidityYears(validity);
        config.setCommonName(etCommonName.getText() != null ? etCommonName.getText().toString().trim() : projectName + " Developer");
        config.setOrganization(etOrganization.getText() != null ? etOrganization.getText().toString().trim() : "GitKey");
        config.setOrganizationalUnit(etOrgUnit.getText() != null ? etOrgUnit.getText().toString().trim() : "Mobile");
        config.setCity(etCity.getText() != null ? etCity.getText().toString().trim() : "San Francisco");
        config.setState(etState.getText() != null ? etState.getText().toString().trim() : "CA");
        config.setCountry(etCountry.getText() != null ? etCountry.getText().toString().trim() : "US");
        config.setLogoUri(selectedLogoUri);

        // Show animated progress overlay
        progressOverlay.show();

        executor.execute(() -> {
            try {
                GeneratedSecrets secrets = KeystoreGenerator.generate(config, (stage, message) -> {
                    mainHandler.post(() -> progressOverlay.updateStage(stage, message));
                    try {
                        // Brief pause between stages for smooth perception of steps
                        Thread.sleep(400);
                    } catch (InterruptedException ignored) {}
                });

                // Save safe project metadata to repository
                ProjectRepository repo = new ProjectRepository(CreateKeyActivity.this);
                String recordId = UUID.randomUUID().toString();
                ProjectRecord record = new ProjectRecord(
                        recordId,
                        config.getProjectName(),
                        config.getAlias(),
                        config.getKeystoreFileName(),
                        System.currentTimeMillis(),
                        "RSA " + config.getKeySize(),
                        secrets.getSha256Fingerprint(),
                        secrets.getSha1Fingerprint(),
                        selectedLogoUri != null ? selectedLogoUri.toString() : null
                );
                record.setCommonName(config.getCommonName());
                record.setOrganization(config.getOrganization());
                record.setCountry(config.getCountry());
                record.setValidityYears(config.getValidityYears());
                record.setMd5Fingerprint(secrets.getMd5Fingerprint());
                record.setKeystoreBase64(secrets.getKeystoreBase64());
                record.setTag("Release");

                // Save keystore bytes in internal vault so user can redownload anytime!
                if (secrets.getKeystoreBytes() != null) {
                    repo.saveKeystoreFile(recordId, secrets.getKeystoreBytes());
                }

                repo.saveProject(record);

                mainHandler.post(() -> {
                    progressOverlay.hide();
                    // Open Result Activity
                    Intent intent = new Intent(CreateKeyActivity.this, KeystoreResultActivity.class);
                    intent.putExtra("generated_secrets", secrets);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressOverlay.hide();
                    Toast.makeText(CreateKeyActivity.this,
                            getString(R.string.error_generation_failed, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class FrameLayoutOverlay {
        private final View root;
        private final TextView tvStage;
        private final TextView tvDetail;

        public FrameLayoutOverlay(View root, TextView tvStage, TextView tvDetail) {
            this.root = root;
            this.tvStage = tvStage;
            this.tvDetail = tvDetail;
        }

        public void show() {
            root.setVisibility(View.VISIBLE);
        }

        public void hide() {
            root.setVisibility(View.GONE);
        }

        public void updateStage(int stage, String message) {
            tvStage.setText(message);
            tvDetail.setText(String.format("Step %d of 5", stage));
        }
    }
}
