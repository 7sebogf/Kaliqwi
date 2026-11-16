package com.example.kaliqwi.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "kaliqwi_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_AVATAR = "avatar";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(int userId, String username, String role, String avatar) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_AVATAR, avatar)
                .apply();
    }

    public boolean isLoggedIn() { return prefs.getInt(KEY_USER_ID, -1) != -1; }

    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, "user"); }
    public String getAvatar() { return prefs.getString(KEY_AVATAR, null); }
    public boolean isAdmin() { return "admin".equals(getRole()); }

    public void setAvatar(String path) { prefs.edit().putString(KEY_AVATAR, path).apply(); }
    public void setUsername(String name) { prefs.edit().putString(KEY_USERNAME, name).apply(); }
    public void setRole(String role) { prefs.edit().putString(KEY_ROLE, role).apply(); }

    public void logout() { prefs.edit().clear().apply(); }
}