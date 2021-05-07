package com.example.project_28_02_2021;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences.Editor editor;

    public static class PreferencePair {
        String key, value;
        public PreferencePair(String key, String value)
        { this.key = key; this.value = value; }
        public String getKey()   { return key; }
        public String getValue() { return value; }
    }

    public PreferenceManager(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_MULTI_PROCESS);
        editor = settings.edit();
        editor.apply();
        editor.commit();
    }

    public void setValueByKey(PreferencePair ...pairs) {
        for (PreferencePair pair : pairs) { editor.putString(pair.getKey(), pair.getValue()); }
        editor.apply();
        editor.commit();
    }

    public final static String SETTINGS_NAME
            = "settings";
    public final static String SORT_KEY
            = "sort_key";
    public final static String SORT_BY_DATE
            = "sort_by_date";
    public final static String SORT_BY_SITE
            = "sort_by_site";
    public final static String SORT_BY_SIZE
            = "sort_by_size";
    public final static String FILTER_KEY
            = "mode_key";
    public final static String FILTER_MODE
            = "filter_mode";
    public final static String NONE_FILTER_MODE
            = "none_filter_mode";
    public final static String CREATED_KEY
            = "created_key";
    public final static String CREATED
            = "created";
    public final static String NONE_CREATED
            = "none_created";
}
