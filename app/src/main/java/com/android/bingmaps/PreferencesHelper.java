package com.android.bingmaps;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {
    private static final String PREFS_NAME = "KML_PREFS";
    private static final String KEY_KML_PATH = "KML_PATH";

    private SharedPreferences sharedPreferences;

    public PreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save KML file path to SharedPreferences
    public void saveKMLPath(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_KML_PATH, path);
        editor.apply();
    }

    // Get KML file path from SharedPreferences
    public String getKMLPath() {
        return sharedPreferences.getString(KEY_KML_PATH, "null");
    }

    // Clear KML file path from SharedPreferences
    public void clearKMLPath() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_KML_PATH);
        editor.apply();
    }
}
