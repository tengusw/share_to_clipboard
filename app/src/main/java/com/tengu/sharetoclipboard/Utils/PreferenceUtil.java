package com.tengu.sharetoclipboard.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tal on 05/09/17.
 */

public class PreferenceUtil {
    private static final String SHOW_TITLE_KEY = "show_title";

    public static void setShowTitle(Context context, boolean value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHOW_TITLE_KEY, value);
        editor.commit();
    }

    public static boolean shouldShowTitle(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(SHOW_TITLE_KEY, true);
    }
}
