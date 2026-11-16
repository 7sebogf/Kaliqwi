package com.example.kaliqwi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "kaliqwi.db";
    private static final int DB_VERSION = 6;

    // users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_AVATAR = "avatar_path";

    // songs
    public static final String TABLE_SONGS = "songs";
    public static final String COL_SONG_ID = "id";
    public static final String COL_SONG_TITLE = "title";
    public static final String COL_SONG_ARTIST = "artist";
    public static final String COL_SONG_GENRE = "genre";
    public static final String COL_SONG_RATING = "rating";
    public static final String COL_SONG_MOOD = "mood";
    public static final String COL_SONG_COMMENT = "comment";
    public static final String COL_SONG_FAVORITE = "favorite";
    public static final String COL_SONG_USER_ID = "user_id";
    public static final String COL_SONG_DATE = "date";
    public static final String COL_SONG_COVER = "cover_path";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL, " +
                COL_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_ROLE + " TEXT NOT NULL DEFAULT '" + ROLE_USER + "', " +
                COL_AVATAR + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SONGS + " (" +
                COL_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SONG_TITLE + " TEXT NOT NULL, " +
                COL_SONG_ARTIST + " TEXT, " +
                COL_SONG_GENRE + " TEXT, " +
                COL_SONG_RATING + " INTEGER DEFAULT 0, " +
                COL_SONG_MOOD + " TEXT, " +
                COL_SONG_COMMENT + " TEXT, " +
                COL_SONG_FAVORITE + " INTEGER DEFAULT 0, " +
                COL_SONG_USER_ID + " INTEGER, " +
                COL_SONG_DATE + " TEXT, " +
                COL_SONG_COVER + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        onCreate(db);
    }

    // USERS

    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + "=?", new String[]{email}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        if (exists) return false;

        int total = getUserCount();
        String role = (total == 0) ? ROLE_ADMIN : ROLE_USER;

        ContentValues v = new ContentValues();
        v.put(COL_USERNAME, username);
        v.put(COL_EMAIL, email);
        v.put(COL_PASSWORD, PasswordUtils.hash(password));
        v.put(COL_ROLE, role);
        return db.insert(TABLE_USERS, null, v) != -1;
    }

    public int loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID, COL_PASSWORD},
                COL_EMAIL + "=?", new String[]{email}, null, null, null);
        int id = -1;
        if (c.moveToFirst()) {
            String stored = c.getString(1);
            if (PasswordUtils.matches(password, stored)) {
                id = c.getInt(0);
            }
        }
        c.close();
        return id;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        User u = null;
        if (c.moveToFirst()) u = cursorToUser(c);
        c.close();
        return u;
    }

    public String getUsernameById(int userId) {
        User u = getUserById(userId);
        return u != null ? u.getUsername() : "";
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, null, null,
                null, null, COL_USER_ID + " ASC");
        while (c.moveToNext()) list.add(cursorToUser(c));
        c.close();
        return list;
    }

    public int getUserCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public int updateProfile(int userId, String username, String email, String avatarPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USERNAME, username);
        v.put(COL_EMAIL, email);
        if (avatarPath != null) v.put(COL_AVATAR, avatarPath);
        return db.update(TABLE_USERS, v,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public int updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_PASSWORD, PasswordUtils.hash(newPassword));
        return db.update(TABLE_USERS, v,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public int updateRole(int userId, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_ROLE, role);
        return db.update(TABLE_USERS, v,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public void deleteUser(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SONGS, COL_SONG_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.delete(TABLE_USERS, COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public boolean isEmailTaken(String email, int exceptUserId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + "=? AND " + COL_USER_ID + "!=?",
                new String[]{email, String.valueOf(exceptUserId)},
                null, null, null);
        boolean taken = c.getCount() > 0;
        c.close();
        return taken;
    }

    private User cursorToUser(Cursor c) {
        User u = new User();
        u.setId(c.getInt(c.getColumnIndexOrThrow(COL_USER_ID)));
        u.setUsername(c.getString(c.getColumnIndexOrThrow(COL_USERNAME)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(COL_EMAIL)));
        u.setPassword(c.getString(c.getColumnIndexOrThrow(COL_PASSWORD)));
        u.setRole(c.getString(c.getColumnIndexOrThrow(COL_ROLE)));
        u.setAvatarPath(c.getString(c.getColumnIndexOrThrow(COL_AVATAR)));
        return u;
    }
}