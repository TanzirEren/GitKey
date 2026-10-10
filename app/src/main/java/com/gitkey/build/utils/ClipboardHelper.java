package com.gitkey.build.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.widget.Toast;

public class ClipboardHelper {

    public static void copy(Context context, String label, String text, boolean isSensitive) {
        if (context == null || text == null) return;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;

        ClipData clip = ClipData.newPlainText(label, text);

        // On Android 13+ (API 33+), flag sensitive content so it's not shown in clipboard overlay
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
            clip.getDescription().setExtras(extras);
        }

        clipboard.setPrimaryClip(clip);

        // Feedback toast
        String msg = isSensitive
                ? "Copied " + label + " (Sensitive). Paste immediately."
                : "Copied " + label + " to clipboard";
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
