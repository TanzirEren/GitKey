package com.gitkey.build.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.gitkey.build.model.ProjectRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    private static final String PREF_NAME = "gitkey_safe_history";
    private static final String KEY_PROJECTS = "saved_project_records";

    private final SharedPreferences prefs;

    public ProjectRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public synchronized List<ProjectRecord> getProjects() {
        List<ProjectRecord> list = new ArrayList<>();
        String json = prefs.getString(KEY_PROJECTS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ProjectRecord r = new ProjectRecord();
                r.setId(obj.optString("id"));
                r.setProjectName(obj.optString("projectName"));
                r.setAlias(obj.optString("alias"));
                r.setFileName(obj.optString("fileName"));
                r.setCreationDateMillis(obj.optLong("creationDateMillis"));
                r.setKeyAlgorithm(obj.optString("keyAlgorithm"));
                r.setSha256Fingerprint(obj.optString("sha256Fingerprint"));
                r.setSha1Fingerprint(obj.optString("sha1Fingerprint"));
                r.setLogoPath(obj.optString("logoPath", null));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveProject(ProjectRecord record) {
        List<ProjectRecord> current = getProjects();
        // Remove existing with same id if any
        for (int i = current.size() - 1; i >= 0; i--) {
            if (current.get(i).getId().equals(record.getId())) {
                current.remove(i);
            }
        }
        // Add at the beginning (most recent first)
        current.add(0, record);
        persist(current);
    }

    public synchronized void deleteProject(String id) {
        List<ProjectRecord> current = getProjects();
        for (int i = current.size() - 1; i >= 0; i--) {
            if (current.get(i).getId().equals(id)) {
                current.remove(i);
                break;
            }
        }
        persist(current);
    }

    public synchronized void clearAll() {
        prefs.edit().remove(KEY_PROJECTS).apply();
    }

    private void persist(List<ProjectRecord> list) {
        try {
            JSONArray array = new JSONArray();
            for (ProjectRecord r : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", r.getId());
                obj.put("projectName", r.getProjectName());
                obj.put("alias", r.getAlias());
                obj.put("fileName", r.getFileName());
                obj.put("creationDateMillis", r.getCreationDateMillis());
                obj.put("keyAlgorithm", r.getKeyAlgorithm());
                obj.put("sha256Fingerprint", r.getSha256Fingerprint());
                obj.put("sha1Fingerprint", r.getSha1Fingerprint());
                if (r.getLogoPath() != null) {
                    obj.put("logoPath", r.getLogoPath());
                }
                array.put(obj);
            }
            prefs.edit().putString(KEY_PROJECTS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
