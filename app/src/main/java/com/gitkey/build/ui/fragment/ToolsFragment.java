package com.gitkey.build.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gitkey.build.R;
import com.gitkey.build.model.ToolItem;
import com.gitkey.build.ui.activity.CreateKeyActivity;
import com.gitkey.build.ui.activity.ToolDetailActivity;
import com.gitkey.build.ui.adapter.ToolsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ToolsFragment extends Fragment implements ToolsAdapter.OnToolClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvTools = view.findViewById(R.id.rvTools);
        rvTools.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ToolItem> tools = new ArrayList<>();
        // 1. Keystore Generator
        tools.add(new ToolItem("tool_keystore_gen", R.string.tool_keystore_gen_title, R.string.tool_keystore_gen_desc, R.drawable.ic_key, R.color.brand_blue));
        // 2. Base64 Encoder
        tools.add(new ToolItem("tool_b64_enc", R.string.tool_b64_enc_title, R.string.tool_b64_enc_desc, R.drawable.ic_code, R.color.brand_cyan));
        // 3. Base64 Decoder
        tools.add(new ToolItem("tool_b64_dec", R.string.tool_b64_dec_title, R.string.tool_b64_dec_desc, R.drawable.ic_code, R.color.brand_cyan));
        // 4. SHA-256 Generator
        tools.add(new ToolItem("tool_sha256", R.string.tool_sha256_title, R.string.tool_sha256_desc, R.drawable.ic_shield, R.color.brand_indigo));
        // 5. SHA-1 Generator
        tools.add(new ToolItem("tool_sha1", R.string.tool_sha1_title, R.string.tool_sha1_desc, R.drawable.ic_shield, R.color.brand_indigo));
        // 6. MD5 Generator
        tools.add(new ToolItem("tool_md5", R.string.tool_md5_title, R.string.tool_md5_desc, R.drawable.ic_shield, R.color.brand_indigo));
        // 7. Keystore Inspector
        tools.add(new ToolItem("tool_inspector", R.string.tool_inspector_title, R.string.tool_inspector_desc, R.drawable.ic_key, R.color.brand_emerald));
        // 8. GitHub Secrets Formatter
        tools.add(new ToolItem("tool_secrets_fmt", R.string.tool_secrets_fmt_title, R.string.tool_secrets_fmt_desc, R.drawable.ic_copy, R.color.brand_amber));
        // 9. JSON Formatter
        tools.add(new ToolItem("tool_json_fmt", R.string.tool_json_fmt_title, R.string.tool_json_fmt_desc, R.drawable.ic_code, R.color.brand_rose));
        // 10. Multi-File Checksum
        tools.add(new ToolItem("tool_checksum", R.string.tool_checksum_title, R.string.tool_checksum_desc, R.drawable.ic_shield, R.color.brand_cyan));
        // 11. Secure Password Generator
        tools.add(new ToolItem("tool_pwd_gen", R.string.tool_pwd_gen_title, R.string.tool_pwd_gen_desc, R.drawable.ic_refresh, R.color.brand_emerald));
        // 12. UUID Generator
        tools.add(new ToolItem("tool_uuid_gen", R.string.tool_uuid_gen_title, R.string.tool_uuid_gen_desc, R.drawable.ic_tools, R.color.brand_blue));

        ToolsAdapter adapter = new ToolsAdapter(tools, this);
        rvTools.setAdapter(adapter);
    }

    @Override
    public void onToolClick(ToolItem tool) {
        if ("tool_keystore_gen".equals(tool.getId())) {
            Intent intent = new Intent(requireContext(), CreateKeyActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(requireContext(), ToolDetailActivity.class);
            intent.putExtra("tool_id", tool.getId());
            intent.putExtra("tool_title_res", tool.getTitleRes());
            intent.putExtra("tool_desc_res", tool.getDescriptionRes());
            startActivity(intent);
        }
    }
}
