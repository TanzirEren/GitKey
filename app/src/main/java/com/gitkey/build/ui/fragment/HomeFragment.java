package com.gitkey.build.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.gitkey.build.ui.activity.MainActivity;
import com.gitkey.build.ui.adapter.ProjectsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeFragment extends Fragment implements ProjectsAdapter.OnProjectActionListener {

    private ProjectRepository repository;
    private ProjectsAdapter adapter;
    private RecyclerView rvRecentProjects;
    private View layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ProjectRepository(requireContext());

        MaterialButton btnCreateKey = view.findViewById(R.id.btnHomeCreateKey);
        TextView tvViewAll = view.findViewById(R.id.tvViewAll);
        rvRecentProjects = view.findViewById(R.id.rvRecentProjects);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        rvRecentProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProjectsAdapter(requireContext(), this);
        rvRecentProjects.setAdapter(adapter);

        btnCreateKey.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CreateKeyActivity.class);
            startActivity(intent);
        });

        tvViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(1);
            }
        });

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
            rvRecentProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvRecentProjects.setVisibility(View.VISIBLE);
            // Show up to 3 recent items on Home screen
            List<ProjectRecord> recent = list.size() > 3 ? list.subList(0, 3) : list;
            adapter.setItems(recent);
        }
    }

    @Override
    public void onProjectClick(ProjectRecord record) {
        // Show summary dialog
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
