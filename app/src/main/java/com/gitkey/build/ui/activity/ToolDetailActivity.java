package com.gitkey.build.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gitkey.build.R;
import com.gitkey.build.crypto.Base64Utils;
import com.gitkey.build.crypto.HashUtils;
import com.gitkey.build.crypto.KeystoreInspector;
import com.gitkey.build.crypto.SecurePasswordGenerator;
import com.gitkey.build.utils.ClipboardHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ToolDetailActivity extends AppCompatActivity {

    private String toolId;
    private FrameLayout container;
    private TextView tvHeaderTitle, tvHeaderDesc;
    private Uri selectedToolFileUri = null;
    private TextView tvFileDisplayTarget = null;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedToolFileUri = uri;
                    if (tvFileDisplayTarget != null) {
                        tvFileDisplayTarget.setText(uri.getLastPathSegment() != null ? uri.getLastPathSegment() : uri.toString());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_detail);

        toolId = getIntent().getStringExtra("tool_id");
        int titleRes = getIntent().getIntExtra("tool_title_res", R.string.tools_title);
        int descRes = getIntent().getIntExtra("tool_desc_res", R.string.tools_subtitle);

        ImageButton btnBack = findViewById(R.id.btnToolBack);
        tvHeaderTitle = findViewById(R.id.tvToolHeaderTitle);
        tvHeaderDesc = findViewById(R.id.tvToolHeaderDesc);
        container = findViewById(R.id.toolContentContainer);

        btnBack.setOnClickListener(v -> finish());
        tvHeaderTitle.setText(titleRes);
        tvHeaderDesc.setText(descRes);

        setupToolView();
    }

    private void setupToolView() {
        if (toolId == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);

        if (toolId.equals("tool_sha256") || toolId.equals("tool_sha1") || toolId.equals("tool_md5")) {
            setupHashTool(inflater);
        } else if (toolId.equals("tool_b64_enc") || toolId.equals("tool_b64_dec")) {
            setupBase64Tool(inflater);
        } else if (toolId.equals("tool_inspector")) {
            setupInspectorTool(inflater);
        } else if (toolId.equals("tool_secrets_fmt")) {
            setupSecretsFmtTool(inflater);
        } else if (toolId.equals("tool_json_fmt")) {
            setupJsonTool(inflater);
        } else if (toolId.equals("tool_checksum")) {
            setupChecksumTool(inflater);
        } else if (toolId.equals("tool_pwd_gen") || toolId.equals("tool_uuid_gen")) {
            setupGeneratorTool(inflater);
        }
    }

    private void setupHashTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_hash, container, true);
        TextInputEditText etInput = v.findViewById(R.id.etHashInput);
        MaterialButton btnSelectFile = v.findViewById(R.id.btnSelectHashFile);
        TextView tvSelectedFile = v.findViewById(R.id.tvSelectedHashFile);
        MaterialButton btnCalc = v.findViewById(R.id.btnCalculateHash);
        View cardResult = v.findViewById(R.id.cardHashResult);
        TextView tvResultLabel = v.findViewById(R.id.tvHashResultLabel);
        TextView tvOutput = v.findViewById(R.id.tvHashOutput);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyHash);

        String algoName = toolId.equals("tool_sha256") ? "SHA-256" :
                (toolId.equals("tool_sha1") ? "SHA-1" : "MD5");
        tvResultLabel.setText(algoName + " HASH OUTPUT");

        btnSelectFile.setOnClickListener(view -> {
            tvFileDisplayTarget = tvSelectedFile;
            filePickerLauncher.launch("*/*");
        });

        btnCalc.setOnClickListener(view -> {
            try {
                String hash;
                if (selectedToolFileUri != null) {
                    InputStream is = getContentResolver().openInputStream(selectedToolFileUri);
                    byte[] digest = HashUtils.digest(is, algoName);
                    hash = HashUtils.toHexString(digest);
                    if (is != null) is.close();
                } else {
                    String text = etInput.getText() != null ? etInput.getText().toString() : "";
                    if (text.isEmpty()) {
                        Toast.makeText(this, "Please enter text or select a file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    byte[] digest = HashUtils.digest(text.getBytes(StandardCharsets.UTF_8), algoName);
                    hash = HashUtils.toHexString(digest);
                }
                tvOutput.setText(hash);
                cardResult.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Hash failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, algoName + " Hash", tvOutput.getText().toString(), false));
    }

    private void setupBase64Tool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_base64, container, true);
        TextView tvLabel = v.findViewById(R.id.tvBase64InputLabel);
        TextInputEditText etInput = v.findViewById(R.id.etBase64Input);
        MaterialButton btnSelectFile = v.findViewById(R.id.btnSelectBase64File);
        TextView tvSelectedFile = v.findViewById(R.id.tvSelectedBase64File);
        MaterialButton btnProcess = v.findViewById(R.id.btnProcessBase64);
        View cardResult = v.findViewById(R.id.cardBase64Result);
        TextView tvResultLabel = v.findViewById(R.id.tvBase64ResultLabel);
        TextView tvOutput = v.findViewById(R.id.tvBase64Output);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyBase64Result);

        boolean isEncode = toolId.equals("tool_b64_enc");
        tvLabel.setText(isEncode ? "Text or File to Encode" : "Base64 String to Decode");
        btnProcess.setText(isEncode ? "ENCODE TO BASE64" : "DECODE FROM BASE64");
        tvResultLabel.setText(isEncode ? "BASE64 RESULT" : "DECODED TEXT");

        btnSelectFile.setOnClickListener(view -> {
            tvFileDisplayTarget = tvSelectedFile;
            filePickerLauncher.launch("*/*");
        });

        btnProcess.setOnClickListener(view -> {
            try {
                if (isEncode) {
                    if (selectedToolFileUri != null) {
                        InputStream is = getContentResolver().openInputStream(selectedToolFileUri);
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);
                        is.close();
                        tvOutput.setText(Base64Utils.encode(buffer));
                    } else {
                        String text = etInput.getText() != null ? etInput.getText().toString() : "";
                        tvOutput.setText(Base64Utils.encodeString(text));
                    }
                } else {
                    String base64 = etInput.getText() != null ? etInput.getText().toString().trim() : "";
                    tvOutput.setText(Base64Utils.decodeToString(base64));
                }
                cardResult.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Base64 operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, "Base64 Result", tvOutput.getText().toString(), false));
    }

    private void setupInspectorTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_inspector, container, true);
        MaterialButton btnSelectFile = v.findViewById(R.id.btnSelectInspectorFile);
        TextView tvSelectedFile = v.findViewById(R.id.tvSelectedInspectorFile);
        TextInputEditText etPass = v.findViewById(R.id.etInspectorPassword);
        MaterialButton btnInspect = v.findViewById(R.id.btnInspectKeystore);
        View cardResult = v.findViewById(R.id.cardInspectorResult);
        TextView tvOutput = v.findViewById(R.id.tvInspectorOutput);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyInspection);

        btnSelectFile.setOnClickListener(view -> {
            tvFileDisplayTarget = tvSelectedFile;
            filePickerLauncher.launch("*/*");
        });

        btnInspect.setOnClickListener(view -> {
            if (selectedToolFileUri == null) {
                Toast.makeText(this, "Please choose a keystore file (.jks / .p12)", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                InputStream is = getContentResolver().openInputStream(selectedToolFileUri);
                String password = etPass.getText() != null ? etPass.getText().toString() : "";
                KeystoreInspector.InspectionResult result = KeystoreInspector.inspect(is, password);
                if (is != null) is.close();

                StringBuilder sb = new StringBuilder();
                sb.append("Keystore Format: ").append(result.keystoreType).append("\n");
                sb.append("Primary Alias: ").append(result.selectedAlias).append("\n");
                sb.append("Has Private Key: ").append(result.hasPrivateKey ? "Yes" : "No").append("\n");
                sb.append("Key Algorithm: ").append(result.algorithm).append("\n");
                sb.append("Signature Algorithm: ").append(result.sigAlgorithm).append("\n");
                sb.append("Valid From: ").append(result.validFrom).append("\n");
                sb.append("Valid Until: ").append(result.validUntil).append("\n\n");
                sb.append("Subject DN:\n").append(result.subjectDN).append("\n\n");
                sb.append("SHA-256 Fingerprint:\n").append(result.sha256Fingerprint).append("\n\n");
                sb.append("SHA-1 Fingerprint:\n").append(result.sha1Fingerprint).append("\n\n");
                sb.append("MD5 Fingerprint:\n").append(result.md5Fingerprint);

                tvOutput.setText(sb.toString());
                cardResult.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Inspection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, "Keystore Metadata", tvOutput.getText().toString(), false));
    }

    private void setupSecretsFmtTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_secrets_fmt, container, true);
        TextInputEditText etB64 = v.findViewById(R.id.etFmtBase64);
        TextInputEditText etKsPass = v.findViewById(R.id.etFmtKeystorePass);
        TextInputEditText etAlias = v.findViewById(R.id.etFmtAlias);
        TextInputEditText etKPass = v.findViewById(R.id.etFmtKeyPass);
        MaterialButton btnFormat = v.findViewById(R.id.btnFormatSecrets);
        View cardResult = v.findViewById(R.id.cardFmtResult);
        TextView tvOutput = v.findViewById(R.id.tvFormattedOutput);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyFormatted);

        btnFormat.setOnClickListener(view -> {
            String b64 = etB64.getText() != null ? etB64.getText().toString().trim() : "";
            String ksPass = etKsPass.getText() != null ? etKsPass.getText().toString() : "";
            String alias = etAlias.getText() != null ? etAlias.getText().toString().trim() : "";
            String kPass = etKPass.getText() != null ? etKPass.getText().toString() : "";

            String formatted = "KEYSTORE_BASE64=" + b64 + "\n" +
                    "KEYSTORE_PASSWORD=" + ksPass + "\n" +
                    "KEY_ALIAS=" + alias + "\n" +
                    "KEY_PASSWORD=" + kPass;

            tvOutput.setText(formatted);
            cardResult.setVisibility(View.VISIBLE);
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, "Formatted Secrets", tvOutput.getText().toString(), true));
    }

    private void setupJsonTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_json, container, true);
        TextInputEditText etInput = v.findViewById(R.id.etJsonInput);
        MaterialButton btnFormat = v.findViewById(R.id.btnFormatJson);
        MaterialButton btnMinify = v.findViewById(R.id.btnMinifyJson);
        View cardResult = v.findViewById(R.id.cardJsonResult);
        TextView tvOutput = v.findViewById(R.id.tvJsonOutput);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyJson);

        btnFormat.setOnClickListener(view -> {
            String text = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            try {
                if (text.startsWith("[")) {
                    JSONArray arr = new JSONArray(text);
                    tvOutput.setText(arr.toString(2));
                } else {
                    JSONObject obj = new JSONObject(text);
                    tvOutput.setText(obj.toString(2));
                }
                cardResult.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnMinify.setOnClickListener(view -> {
            String text = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            try {
                if (text.startsWith("[")) {
                    JSONArray arr = new JSONArray(text);
                    tvOutput.setText(arr.toString());
                } else {
                    JSONObject obj = new JSONObject(text);
                    tvOutput.setText(obj.toString());
                }
                cardResult.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, "JSON Output", tvOutput.getText().toString(), false));
    }

    private void setupChecksumTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_checksum, container, true);
        MaterialButton btnSelectFile = v.findViewById(R.id.btnSelectChecksumFile);
        TextView tvSelectedFile = v.findViewById(R.id.tvChecksumFileName);
        View cardResults = v.findViewById(R.id.cardChecksumResults);
        TextView tvSha256 = v.findViewById(R.id.tvChecksumSha256);
        TextView tvSha1 = v.findViewById(R.id.tvChecksumSha1);
        TextView tvMd5 = v.findViewById(R.id.tvChecksumMd5);

        MaterialButton btnCopySha256 = v.findViewById(R.id.btnCopySha256);
        MaterialButton btnCopySha1 = v.findViewById(R.id.btnCopySha1);
        MaterialButton btnCopyMd5 = v.findViewById(R.id.btnCopyMd5);

        btnSelectFile.setOnClickListener(view -> {
            tvFileDisplayTarget = tvSelectedFile;
            filePickerLauncher.launch("*/*");
        });

        btnSelectFile.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (selectedToolFileUri != null && cardResults.getVisibility() != View.VISIBLE) {
                try {
                    InputStream is256 = getContentResolver().openInputStream(selectedToolFileUri);
                    InputStream is1 = getContentResolver().openInputStream(selectedToolFileUri);
                    InputStream isMd5 = getContentResolver().openInputStream(selectedToolFileUri);

                    tvSha256.setText(HashUtils.toHexString(HashUtils.digest(is256, "SHA-256")));
                    tvSha1.setText(HashUtils.toHexString(HashUtils.digest(is1, "SHA-1")));
                    tvMd5.setText(HashUtils.toHexString(HashUtils.digest(isMd5, "MD5")));

                    if (is256 != null) is256.close();
                    if (is1 != null) is1.close();
                    if (isMd5 != null) isMd5.close();

                    cardResults.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Toast.makeText(this, "Checksum failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCopySha256.setOnClickListener(view -> ClipboardHelper.copy(this, "SHA-256 Checksum", tvSha256.getText().toString(), false));
        btnCopySha1.setOnClickListener(view -> ClipboardHelper.copy(this, "SHA-1 Checksum", tvSha1.getText().toString(), false));
        btnCopyMd5.setOnClickListener(view -> ClipboardHelper.copy(this, "MD5 Checksum", tvMd5.getText().toString(), false));
    }

    private void setupGeneratorTool(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.layout_tool_generator, container, true);
        TextView tvOptTitle = v.findViewById(R.id.tvGenOptionTitle);
        TextInputEditText etLength = v.findViewById(R.id.etGenLength);
        MaterialButton btnGen = v.findViewById(R.id.btnGenerateValue);
        View cardResult = v.findViewById(R.id.cardGenResult);
        TextView tvOutput = v.findViewById(R.id.tvGenOutput);
        MaterialButton btnCopy = v.findViewById(R.id.btnCopyGenValue);

        boolean isUuid = toolId.equals("tool_uuid_gen");
        tvOptTitle.setText(isUuid ? "UUID Generation Count" : "Password Length");
        etLength.setText(isUuid ? "1" : "16");

        btnGen.setOnClickListener(view -> {
            int count = 16;
            try {
                if (etLength.getText() != null) {
                    count = Integer.parseInt(etLength.getText().toString().trim());
                }
            } catch (Exception ignored) {}

            if (isUuid) {
                StringBuilder sb = new StringBuilder();
                int num = Math.min(Math.max(1, count), 50);
                for (int i = 0; i < num; i++) {
                    sb.append(UUID.randomUUID().toString());
                    if (i < num - 1) sb.append("\n");
                }
                tvOutput.setText(sb.toString());
            } else {
                int len = Math.min(Math.max(8, count), 128);
                tvOutput.setText(SecurePasswordGenerator.generatePassword(len));
            }
            cardResult.setVisibility(View.VISIBLE);
        });

        btnCopy.setOnClickListener(view -> ClipboardHelper.copy(this, isUuid ? "UUID" : "Secure Password", tvOutput.getText().toString(), !isUuid));
    }
}
