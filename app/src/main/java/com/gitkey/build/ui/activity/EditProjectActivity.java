package com.gitkey.build.ui.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.model.ProjectRecord;
import com.gitkey.build.storage.ProjectRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;

public class EditProjectActivity extends AppCompatActivity {

    private ProjectRepository repository;
    private ProjectRecord project;

    private ImageView ivEditLogo;
    private MaterialButton btnChooseLogo, btnRemoveLogo, btnSaveTop, btnSaveChanges, btnCancel;
    private TextInputEditText etProjectName, etKeyAlias, etFileName, etProjectTag;
    private TextInputEditText etValidityYears, etCommonName, etOrganization, etCountry, etNotes;
    private SwitchMaterial switchFavorite;
    private TextView chipTagProduction, chipTagBeta, chipTagStaging, chipTagInternal;

    private String selectedLogoUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedLogoUri = uri.toString();
                    loadLogo(selectedLogoUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        repository = new ProjectRepository(this);
        String projectId = getIntent().getStringExtra("project_id");
        if (projectId != null) {
            project = repository.getProjectById(projectId);
        }

        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindData();
        setupListeners();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSaveTop = findViewById(R.id.btnSaveTop);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);

        ivEditLogo = findViewById(R.id.ivEditLogo);
        btnChooseLogo = findViewById(R.id.btnChooseLogo);
        btnRemoveLogo = findViewById(R.id.btnRemoveLogo);

        etProjectName = findViewById(R.id.etProjectName);
        etKeyAlias = findViewById(R.id.etKeyAlias);
        etFileName = findViewById(R.id.etFileName);
        etProjectTag = findViewById(R.id.etProjectTag);

        chipTagProduction = findViewById(R.id.chipTagProduction);
        chipTagBeta = findViewById(R.id.chipTagBeta);
        chipTagStaging = findViewById(R.id.chipTagStaging);
        chipTagInternal = findViewById(R.id.chipTagInternal);

        switchFavorite = findViewById(R.id.switchFavorite);

        etValidityYears = findViewById(R.id.etValidityYears);
        etCommonName = findViewById(R.id.etCommonName);
        etOrganization = findViewById(R.id.etOrganization);
        etCountry = findViewById(R.id.etCountry);
        etNotes = findViewById(R.id.etNotes);
    }

    private void bindData() {
        etProjectName.setText(project.getProjectName());
        etKeyAlias.setText(project.getAlias());
        etFileName.setText(project.getFileName());
        etProjectTag.setText(project.getTag() != null ? project.getTag() : "Production");
        switchFavorite.setChecked(project.isFavorite());

        etValidityYears.setText(String.valueOf(project.getValidityYears() > 0 ? project.getValidityYears() : 25));
        etCommonName.setText(project.getCommonName());
        etOrganization.setText(project.getOrganization());
        etCountry.setText(project.getCountry());
        etNotes.setText(project.getNotes());

        selectedLogoUri = project.getLogoPath();
        loadLogo(selectedLogoUri);
    }

    private void loadLogo(String uriStr) {
        if (uriStr != null) {
            try {
                Uri uri = Uri.parse(uriStr);
                InputStream is = getContentResolver().openInputStream(uri);
                if (is != null) {
                    ivEditLogo.setImageBitmap(BitmapFactory.decodeStream(is));
                    ivEditLogo.setPadding(0, 0, 0, 0);
                    is.close();
                    return;
                }
            } catch (Exception ignored) {}
        }
        ivEditLogo.setImageResource(R.drawable.ic_gitkey_logo);
        ivEditLogo.setPadding(20, 20, 20, 20);
    }

    private void setupListeners() {
        btnChooseLogo.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnRemoveLogo.setOnClickListener(v -> {
            selectedLogoUri = null;
            loadLogo(null);
        });

        chipTagProduction.setOnClickListener(v -> etProjectTag.setText("Production"));
        chipTagBeta.setOnClickListener(v -> etProjectTag.setText("Beta"));
        chipTagStaging.setOnClickListener(v -> etProjectTag.setText("Staging"));
        chipTagInternal.setOnClickListener(v -> etProjectTag.setText("Internal"));

        View.OnClickListener saveListener = v -> saveProjectChanges();
        btnSaveTop.setOnClickListener(saveListener);
        btnSaveChanges.setOnClickListener(saveListener);
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveProjectChanges() {
        String name = etProjectName.getText() != null ? etProjectName.getText().toString().trim() : "";
        String alias = etKeyAlias.getText() != null ? etKeyAlias.getText().toString().trim() : "";
        String fileName = etFileName.getText() != null ? etFileName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etProjectName.setError("Project name cannot be empty");
            etProjectName.requestFocus();
            return;
        }

        if (alias.isEmpty()) {
            etKeyAlias.setError("Key alias cannot be empty");
            etKeyAlias.requestFocus();
            return;
        }

        if (fileName.isEmpty()) {
            fileName = name.toLowerCase().replaceAll("[^a-z0-9_-]", "_") + "-release.jks";
        } else if (!fileName.endsWith(".jks") && !fileName.endsWith(".keystore")) {
            fileName = fileName + ".jks";
        }

        int validity = 25;
        try {
            if (etValidityYears.getText() != null) {
                validity = Integer.parseInt(etValidityYears.getText().toString().trim());
            }
        } catch (NumberFormatException ignored) {}

        String tag = etProjectTag.getText() != null ? etProjectTag.getText().toString().trim() : "";
        String cn = etCommonName.getText() != null ? etCommonName.getText().toString().trim() : "";
        String org = etOrganization.getText() != null ? etOrganization.getText().toString().trim() : "";
        String country = etCountry.getText() != null ? etCountry.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        project.setProjectName(name);
        project.setAlias(alias);
        project.setFileName(fileName);
        project.setTag(tag);
        project.setFavorite(switchFavorite.isChecked());
        project.setValidityYears(validity);
        project.setCommonName(cn);
        project.setOrganization(org);
        project.setCountry(country);
        project.setNotes(notes);
        project.setLogoPath(selectedLogoUri);

        repository.updateProject(project);

        Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
