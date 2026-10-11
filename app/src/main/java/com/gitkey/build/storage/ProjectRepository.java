package com.gitkey.build.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.gitkey.build.crypto.Base64Utils;
import com.gitkey.build.model.ProjectRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectRepository {
    private static final String PREF_NAME = "gitkey_safe_history";
    private static final String KEY_PROJECTS = "saved_project_records";
    private static final String KEYSTORE_DIR_NAME = "vault_keystores";

    private final Context context;
    private final SharedPreferences prefs;
    private final File keystoreDir;

    public ProjectRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.keystoreDir = new File(this.context.getFilesDir(), KEYSTORE_DIR_NAME);
        if (!keystoreDir.exists()) {
            keystoreDir.mkdirs();
        }
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
                r.setMd5Fingerprint(obj.optString("md5Fingerprint", ""));
                r.setLogoPath(obj.optString("logoPath", null));
                r.setNotes(obj.optString("notes", ""));
                r.setTag(obj.optString("tag", ""));
                r.setValidityYears(obj.optInt("validityYears", 25));
                r.setCommonName(obj.optString("commonName", ""));
                r.setOrganization(obj.optString("organization", ""));
                r.setCountry(obj.optString("country", ""));
                r.setKeystoreBase64(obj.optString("keystoreBase64", ""));
                r.setFavorite(obj.optBoolean("favorite", false));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized ProjectRecord getProjectById(String id) {
        if (id == null) return null;
        for (ProjectRecord r : getProjects()) {
            if (id.equals(r.getId())) {
                return r;
            }
        }
        return null;
    }

    public synchronized void saveProject(ProjectRecord record) {
        List<ProjectRecord> current = getProjects();
        for (int i = current.size() - 1; i >= 0; i--) {
            if (current.get(i).getId().equals(record.getId())) {
                current.remove(i);
            }
        }
        current.add(0, record);
        persist(current);
    }

    public synchronized void updateProject(ProjectRecord record) {
        List<ProjectRecord> current = getProjects();
        boolean found = false;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId().equals(record.getId())) {
                current.set(i, record);
                found = true;
                break;
            }
        }
        if (!found) {
            current.add(0, record);
        }
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

        // Delete associated stored keystore file if present
        File file = getKeystoreFile(id);
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public synchronized void clearAll() {
        prefs.edit().remove(KEY_PROJECTS).apply();
        if (keystoreDir.exists()) {
            File[] files = keystoreDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    public synchronized boolean saveKeystoreFile(String id, byte[] bytes) {
        if (id == null || bytes == null) return false;
        try {
            File file = new File(keystoreDir, id + ".jks");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File getKeystoreFile(String id) {
        if (id == null) return null;
        File file = new File(keystoreDir, id + ".jks");
        return file.exists() ? file : null;
    }

    public byte[] getKeystoreBytes(String id) {
        File file = getKeystoreFile(id);
        if (file == null || !file.exists()) return null;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getKeystoreBase64(String id) {
        ProjectRecord r = getProjectById(id);
        if (r != null && r.getKeystoreBase64() != null && !r.getKeystoreBase64().isEmpty()) {
            return r.getKeystoreBase64();
        }
        byte[] bytes = getKeystoreBytes(id);
        if (bytes != null) {
            return Base64Utils.encode(bytes);
        }
        return null;
    }

    public synchronized String backupAllProjectsAsJson() {
        return prefs.getString(KEY_PROJECTS, "[]");
    }

    public synchronized int restoreProjectsFromJson(String json) {
        if (json == null || json.trim().isEmpty()) return 0;
        try {
            JSONArray array = new JSONArray(json);
            List<ProjectRecord> current = getProjects();
            int importedCount = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id");
                boolean exists = false;
                for (ProjectRecord existing : current) {
                    if (existing.getId().equals(id)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    ProjectRecord r = new ProjectRecord();
                    r.setId(id);
                    r.setProjectName(obj.optString("projectName"));
                    r.setAlias(obj.optString("alias"));
                    r.setFileName(obj.optString("fileName"));
                    r.setCreationDateMillis(obj.optLong("creationDateMillis"));
                    r.setKeyAlgorithm(obj.optString("keyAlgorithm"));
                    r.setSha256Fingerprint(obj.optString("sha256Fingerprint"));
                    r.setSha1Fingerprint(obj.optString("sha1Fingerprint"));
                    r.setMd5Fingerprint(obj.optString("md5Fingerprint", ""));
                    r.setLogoPath(obj.optString("logoPath", null));
                    r.setNotes(obj.optString("notes", ""));
                    r.setTag(obj.optString("tag", ""));
                    r.setValidityYears(obj.optInt("validityYears", 25));
                    r.setCommonName(obj.optString("commonName", ""));
                    r.setOrganization(obj.optString("organization", ""));
                    r.setCountry(obj.optString("country", ""));
                    r.setKeystoreBase64(obj.optString("keystoreBase64", ""));
                    r.setFavorite(obj.optBoolean("favorite", false));
                    current.add(0, r);
                    importedCount++;
                }
            }
            if (importedCount > 0) {
                persist(current);
            }
            return importedCount;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
                obj.put("md5Fingerprint", r.getMd5Fingerprint());
                if (r.getLogoPath() != null) {
                    obj.put("logoPath", r.getLogoPath());
                }
                obj.put("notes", r.getNotes());
                obj.put("tag", r.getTag());
                obj.put("validityYears", r.getValidityYears());
                obj.put("commonName", r.getCommonName());
                obj.put("organization", r.getOrganization());
                obj.put("country", r.getCountry());
                if (r.getKeystoreBase64() != null) {
                    obj.put("keystoreBase64", r.getKeystoreBase64());
                }
                obj.put("favorite", r.isFavorite());
                array.put(obj);
            }
            prefs.edit().putString(KEY_PROJECTS, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
