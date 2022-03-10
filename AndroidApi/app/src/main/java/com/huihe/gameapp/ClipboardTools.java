package com.huihe.gameapp;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardTools {

    public static void copyTextToClipboard(Context activity, String str) throws Exception {
        ClipboardManager clipboard = (ClipboardManager)activity.getSystemService("clipboard");
        ClipData textCd = ClipData.newPlainText(null, str);
        clipboard.setPrimaryClip(textCd);
    }

    public static String getTextFromClipboard(Context activity) {
        ClipboardManager clipboard = (ClipboardManager)activity.getSystemService("clipboard");
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard
                .getPrimaryClipDescription().hasMimeType("text/plain")) {
            ClipData cdText = clipboard.getPrimaryClip();
            ClipData.Item item = cdText.getItemAt(0);
            return item.getText().toString();
        }
        return "null";
    }

}
