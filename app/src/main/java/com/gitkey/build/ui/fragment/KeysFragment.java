package com.gitkey.build.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gitkey.build.R;
import com.gitkey.build.model.ProjectRecord;
import com.gitkey.build.storage.ProjectRepository;
import com.gitkey.build.ui.adapter.ProjectsAdapter;

import java.util.List;

public class KeysFragment extends Fragment implements ProjectsAdapter.OnProjectActionListener {

    private ProjectRepository repository;
    private ProjectsAdapter adapter;
    private RecyclerView rvAllProjects;
    private View layoutEmptyKeys;

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

        rvAllProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProjectsAdapter(requireContext(), this);
        rvAllProjects.setAdapter(adapter);

        View btnEmptyCreate = view.findViewById(R.id.btnEmptyCreateKeyFromKeys);
        if (btnEmptyCreate != null) {
            btnEmptyCreate.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(requireActivity(), com.gitkey.build.ui.activity.CreateKeyActivity.class);
                startActivity(intent);
            });
        }

        loadProjects();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        List<ProjectRecord> list = repository.getProjects();
        if (list.isEmpty()) {
            rvAllProjects.setVisibility(View.GONE);
            layoutEmptyKeys.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyKeys.setVisibility(View.GONE);
            rvAllProjects.setVisibility(View.VISIBLE);
            adapter.setItems(list);
        }
    }

    @Override
    public void onProjectClick(ProjectRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle(record.getProjectName())
                .setMessage("Keystore: " + record.getFileName() + "\n" +
                        "Alias: " + record.getAlias() + "\n" +
                        "Algorithm: " + record.getKeyAlgorithm() + "\n\n" +
                        "SHA-256 Fingerprint:\n" + record.getSha256Fingerprint() + "\n\n" +
                        "SHA-1 Fingerprint:\n" + record.getSha1Fingerprint())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onProjectDelete(ProjectRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Record?")
                .setMessage("Remove " + record.getProjectName() + " from saved projects history?")
                .setPositiveButton("Delete", (d, w) -> {
                    repository.deleteProject(record.getId());
                    loadProjects();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
