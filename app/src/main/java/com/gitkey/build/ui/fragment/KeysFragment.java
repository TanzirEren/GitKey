package com.gitkey.build.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gitkey.build.R;
import com.gitkey.build.model.ProjectRecord;
import com.gitkey.build.storage.ProjectRepository;
import com.gitkey.build.ui.activity.CreateKeyActivity;
import com.gitkey.build.ui.activity.ProjectDetailActivity;
import com.gitkey.build.ui.adapter.ProjectsAdapter;

import java.util.ArrayList;
import java.util.List;

public class KeysFragment extends Fragment implements ProjectsAdapter.OnProjectActionListener {

    private ProjectRepository repository;
    private ProjectsAdapter adapter;
    private RecyclerView rvAllProjects;
    private View layoutEmptyKeys;
    private EditText etSearchProjects;
    private TextView tvKeysSubtitle;
    private TextView chipFilterAll, chipFilterStarred, chipFilterActive, chipFilterExpired;

    private List<ProjectRecord> allProjects = new ArrayList<>();
    private String currentQuery = "";
    private int currentFilter = 0; // 0=All, 1=Starred, 2=Active, 3=Expired

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_keys, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ProjectRepository(requireContext());
        rvAllProjects = view.findViewById(R.id.rvAllProjects);
        layoutEmptyKeys = view.findViewById(R.id.layoutEmptyKeys);
        tvKeysSubtitle = view.findViewById(R.id.tvKeysSubtitle);
        etSearchProjects = view.findViewById(R.id.etSearchProjects);

        chipFilterAll = view.findViewById(R.id.chipFilterAll);
        chipFilterStarred = view.findViewById(R.id.chipFilterStarred);
        chipFilterActive = view.findViewById(R.id.chipFilterActive);
        chipFilterExpired = view.findViewById(R.id.chipFilterExpired);

        rvAllProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProjectsAdapter(requireContext(), this);
        rvAllProjects.setAdapter(adapter);

        view.findViewById(R.id.btnCreateKeyTop).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CreateKeyActivity.class);
            startActivity(intent);
        });

        View btnEmptyCreate = view.findViewById(R.id.btnEmptyCreateKeyFromKeys);
        if (btnEmptyCreate != null) {
            btnEmptyCreate.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), CreateKeyActivity.class);
                startActivity(intent);
            });
        }

        setupSearchAndFilters();
        loadProjects();
    }

    private void setupSearchAndFilters() {
        etSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s != null ? s.toString().trim() : "";
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipFilterAll.setOnClickListener(v -> setFilter(0));
        chipFilterStarred.setOnClickListener(v -> setFilter(1));
        chipFilterActive.setOnClickListener(v -> setFilter(2));
        chipFilterExpired.setOnClickListener(v -> setFilter(3));
    }

    private void setFilter(int filterIndex) {
        currentFilter = filterIndex;
        updateFilterChipStyles();
        applyFilterAndSearch();
    }

    private void updateFilterChipStyles() {
        int primary = requireContext().getColor(R.color.brand_blue);
        int muted = requireContext().getColor(R.color.text_tertiary_light);

        chipFilterAll.setTextColor(currentFilter == 0 ? primary : muted);
        chipFilterStarred.setTextColor(currentFilter == 1 ? primary : muted);
        chipFilterActive.setTextColor(currentFilter == 2 ? primary : muted);
        chipFilterExpired.setTextColor(currentFilter == 3 ? primary : muted);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        allProjects = repository.getProjects();
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        if (allProjects.isEmpty()) {
            rvAllProjects.setVisibility(View.GONE);
            layoutEmptyKeys.setVisibility(View.VISIBLE);
            tvKeysSubtitle.setText("0 keystores in secure vault");
            return;
        }

        List<ProjectRecord> filtered = new ArrayList<>();
        String q = currentQuery.toLowerCase();

        for (ProjectRecord p : allProjects) {
            // Apply category filter
            if (currentFilter == 1 && !p.isFavorite()) continue;
            if (currentFilter == 2 && p.isExpired()) continue;
            if (currentFilter == 3 && !p.isExpired()) continue;

            // Apply search query
            if (!q.isEmpty()) {
                boolean matchesName = p.getProjectName() != null && p.getProjectName().toLowerCase().contains(q);
                boolean matchesAlias = p.getAlias() != null && p.getAlias().toLowerCase().contains(q);
                boolean matchesFile = p.getFileName() != null && p.getFileName().toLowerCase().contains(q);
                boolean matchesTag = p.getTag() != null && p.getTag().toLowerCase().contains(q);
                boolean matchesSha = p.getSha256Fingerprint() != null && p.getSha256Fingerprint().toLowerCase().contains(q);

                if (!matchesName && !matchesAlias && !matchesFile && !matchesTag && !matchesSha) {
                    continue;
                }
            }

            filtered.add(p);
        }

        if (filtered.isEmpty()) {
            rvAllProjects.setVisibility(View.GONE);
            layoutEmptyKeys.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyKeys.setVisibility(View.GONE);
            rvAllProjects.setVisibility(View.VISIBLE);
            adapter.setItems(filtered);
        }

        tvKeysSubtitle.setText(String.format("%d of %d keystores found", filtered.size(), allProjects.size()));
    }

    @Override
    public void onProjectClick(ProjectRecord record) {
        Intent intent = new Intent(requireContext(), ProjectDetailActivity.class);
        intent.putExtra("project_id", record.getId());
        intent.putExtra("project_record", record);
        startActivity(intent);
    }

    @Override
    public void onProjectDelete(ProjectRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Project?")
                .setMessage("Remove " + record.getProjectName() + " and its keystore from your device?")
                .setPositiveButton("Delete", (d, w) -> {
                    repository.deleteProject(record.getId());
                    loadProjects();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
