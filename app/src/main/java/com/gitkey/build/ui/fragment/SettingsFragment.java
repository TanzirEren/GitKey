package com.gitkey.build.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.gitkey.build.R;
import com.gitkey.build.storage.FileExportHelper;
import com.gitkey.build.storage.ProjectRepository;
import com.gitkey.build.utils.PreferencesManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SettingsFragment extends Fragment {

    private PreferencesManager prefs;
    private ProjectRepository repository;

    private final ActivityResultLauncher<String> backupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    try {
                        String json = repository.backupAllProjectsAsJson();
                        FileExportHelper.writeStringToUri(requireContext(), uri, json);
                        Toast.makeText(requireContext(), "Vault backup saved successfully!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> restoreLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream is = requireContext().getContentResolver().openInputStream(uri);
                        if (is != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            reader.close();
                            is.close();

                            int count = repository.restoreProjectsFromJson(sb.toString());
                            if (count >= 0) {
                                Toast.makeText(requireContext(), "Restored " + count + " new project(s) to vault!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Invalid or corrupted backup JSON file", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new PreferencesManager(requireContext());
        repository = new ProjectRepository(requireContext());

        RadioGroup rgTheme = view.findViewById(R.id.rgTheme);
        RadioButton rbThemeSystem = view.findViewById(R.id.rbThemeSystem);
        RadioButton rbThemeLight = view.findViewById(R.id.rbThemeLight);
        RadioButton rbThemeDark = view.findViewById(R.id.rbThemeDark);

        int currentTheme = prefs.getThemeMode();
        if (currentTheme == 1) {
            rbThemeLight.setChecked(true);
        } else if (currentTheme == 2) {
            rbThemeDark.setChecked(true);
        } else {
            rbThemeSystem.setChecked(true);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = 0;
            if (checkedId == R.id.rbThemeLight) {
                newMode = 1;
            } else if (checkedId == R.id.rbThemeDark) {
                newMode = 2;
            }
            prefs.setThemeMode(newMode);
        });

        SwitchMaterial switchConfirmExport = view.findViewById(R.id.switchConfirmExport);
        switchConfirmExport.setChecked(prefs.isRequireExportConfirmation());
        switchConfirmExport.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setRequireExportConfirmation(isChecked);
        });

        SwitchMaterial switchHideSecrets = view.findViewById(R.id.switchHideSecrets);
        switchHideSecrets.setChecked(prefs.isHideSecretsByDefault());
        switchHideSecrets.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setHideSecretsByDefault(isChecked);
        });

        // Backup Vault
        View layoutBackupVault = view.findViewById(R.id.layoutBackupVault);
        if (layoutBackupVault != null) {
            layoutBackupVault.setOnClickListener(v -> backupLauncher.launch("gitkey-vault-backup.json"));
        }

        // Restore Vault
        View layoutRestoreVault = view.findViewById(R.id.layoutRestoreVault);
        if (layoutRestoreVault != null) {
            layoutRestoreVault.setOnClickListener(v -> restoreLauncher.launch("*/*"));
        }

        // Clear History
        View layoutClearHistory = view.findViewById(R.id.layoutClearHistory);
        layoutClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all saved project records? This will remove all project metadata and vault keystores from your device.")
                    .setPositiveButton("Clear All", (d, w) -> {
                        repository.clearAll();
                        Toast.makeText(requireContext(), "Project history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
