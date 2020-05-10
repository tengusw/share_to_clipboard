package com.tengu.sharetoclipboard.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tal on 05/09/17.
 */

public class PreferenceUtil {
    private static final String SHOW_TITLE_KEY = "show_title";
    private static final String DISPLAY_NOTIFICATION_KEY = "display_notification";

    public static void setShowTitle(Context context, boolean value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHOW_TITLE_KEY, value);
        editor.apply();
    }

    public static boolean shouldShowTitle(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(SHOW_TITLE_KEY, true);
    }

    public static void setDisplayNotification(Context context, boolean value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(DISPLAY_NOTIFICATION_KEY, value);
        editor.apply();
    }

    public static boolean shouldDisplayNotification(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(DISPLAY_NOTIFICATION_KEY, true);
    }
}
