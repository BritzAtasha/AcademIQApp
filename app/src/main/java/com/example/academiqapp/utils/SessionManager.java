package com.example.academiqapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_USER_ID = "user_id";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // -----------------------
    // 🔹 Save full session atomically
    // -----------------------
    public void saveSession(boolean isLoggedIn, String userId, String username, boolean isAdmin) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply(); // commit all changes at once
    }

    // -----------------------
    // LOGIN STATUS
    // -----------------------
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // -----------------------
    // USERNAME
    // -----------------------
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "User");
    }

    // -----------------------
    // USER TYPE
    // -----------------------
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    // -----------------------
    // USER ID
    // -----------------------
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // -----------------------
    // LOGOUT
    // -----------------------
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
