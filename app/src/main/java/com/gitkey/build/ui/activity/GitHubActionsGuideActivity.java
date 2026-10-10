package com.gitkey.build.ui.activity;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.storage.FileExportHelper;
import com.gitkey.build.utils.ClipboardHelper;
import com.google.android.material.button.MaterialButton;

public class GitHubActionsGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github_guide);

        ImageButton btnGuideBack = findViewById(R.id.btnGuideBack);
        MaterialButton btnCopyWorkflow = findViewById(R.id.btnCopyWorkflowTemplate);

        btnGuideBack.setOnClickListener(v -> finish());

        btnCopyWorkflow.setOnClickListener(v -> {
            String yaml = FileExportHelper.generateWorkflowYaml("AndroidApp", "release_key", "release-key.jks");
            ClipboardHelper.copy(this, "GitHub Actions Workflow YAML", yaml, false);
        });
    }
}
