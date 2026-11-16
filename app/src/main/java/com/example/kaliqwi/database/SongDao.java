package com.example.kaliqwi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kaliqwi.models.Song;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SongDao {

    private final DatabaseHelper dbHelper;

    public SongDao(Context context) { dbHelper = new DatabaseHelper(context); }

    public long addSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_SONG_TITLE, song.getTitle());
        v.put(DatabaseHelper.COL_SONG_ARTIST, song.getArtist());
        v.put(DatabaseHelper.COL_SONG_GENRE, song.getGenre());
        v.put(DatabaseHelper.COL_SONG_RATING, song.getRating());
        v.put(DatabaseHelper.COL_SONG_MOOD, song.getMood());
        v.put(DatabaseHelper.COL_SONG_COMMENT, song.getComment());
        v.put(DatabaseHelper.COL_SONG_FAVORITE, song.isFavorite() ? 1 : 0);
        v.put(DatabaseHelper.COL_SONG_USER_ID, song.getUserId());
        v.put(DatabaseHelper.COL_SONG_DATE, song.getDate());
        v.put(DatabaseHelper.COL_SONG_COVER, song.getCoverPath());
        long id = db.insert(DatabaseHelper.TABLE_SONGS, null, v);
        android.util.Log.d("KALIQWI", "addSong id=" + id
                + " userId=" + song.getUserId() + " title=" + song.getTitle());
        return id;
    }

    public List<Song> getSongsByUser(int userId) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_SONGS, null,
                DatabaseHelper.COL_SONG_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.COL_SONG_ID + " DESC");
        while (c.moveToNext()) list.add(cursorToSong(c));
        c.close();
        android.util.Log.d("KALIQWI", "getSongsByUser userId=" + userId
                + " found=" + list.size());
        return list;
    }

    public List<Song> getFavorites(int userId) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_SONGS, null,
                DatabaseHelper.COL_SONG_USER_ID + "=? AND " +
                        DatabaseHelper.COL_SONG_FAVORITE + "=1",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.COL_SONG_ID + " DESC");
        while (c.moveToNext()) list.add(cursorToSong(c));
        c.close();
        return list;
    }

    public List<Song> search(int userId, String query) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String like = "%" + query + "%";
        Cursor c = db.query(DatabaseHelper.TABLE_SONGS, null,
                DatabaseHelper.COL_SONG_USER_ID + "=? AND (" +
                        DatabaseHelper.COL_SONG_TITLE + " LIKE ? OR " +
                        DatabaseHelper.COL_SONG_ARTIST + " LIKE ?)",
                new String[]{String.valueOf(userId), like, like},
                null, null, DatabaseHelper.COL_SONG_ID + " DESC");
        while (c.moveToNext()) list.add(cursorToSong(c));
        c.close();
        return list;
    }

    public Song getSongById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_SONGS, null,
                DatabaseHelper.COL_SONG_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Song s = null;
        if (c.moveToFirst()) s = cursorToSong(c);
        c.close();
        return s;
    }

    public int updateSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_SONG_TITLE, song.getTitle());
        v.put(DatabaseHelper.COL_SONG_ARTIST, song.getArtist());
        v.put(DatabaseHelper.COL_SONG_GENRE, song.getGenre());
        v.put(DatabaseHelper.COL_SONG_RATING, song.getRating());
        v.put(DatabaseHelper.COL_SONG_MOOD, song.getMood());
        v.put(DatabaseHelper.COL_SONG_COMMENT, song.getComment());
        v.put(DatabaseHelper.COL_SONG_FAVORITE, song.isFavorite() ? 1 : 0);
        v.put(DatabaseHelper.COL_SONG_COVER, song.getCoverPath());
        return db.update(DatabaseHelper.TABLE_SONGS, v,
                DatabaseHelper.COL_SONG_ID + "=?",
                new String[]{String.valueOf(song.getId())});
    }

    public void toggleFavorite(int songId, boolean favorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COL_SONG_FAVORITE, favorite ? 1 : 0);
        db.update(DatabaseHelper.TABLE_SONGS, v,
                DatabaseHelper.COL_SONG_ID + "=?",
                new String[]{String.valueOf(songId)});
    }

    public void deleteSong(int songId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SONGS,
                DatabaseHelper.COL_SONG_ID + "=?",
                new String[]{String.valueOf(songId)});
    }

    public float getAverageRating(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT AVG(" + DatabaseHelper.COL_SONG_RATING + ") FROM " +
                        DatabaseHelper.TABLE_SONGS + " WHERE " +
                        DatabaseHelper.COL_SONG_USER_ID + "=? AND " +
                        DatabaseHelper.COL_SONG_RATING + ">0",
                new String[]{String.valueOf(userId)});
        float avg = 0f;
        if (c.moveToFirst()) avg = c.getFloat(0);
        c.close();
        return avg;
    }

    // Статистика
    public Map<String, Integer> getGenreStats(int userId, int limit) {
        return topBy(userId, DatabaseHelper.COL_SONG_GENRE, limit);
    }
    public Map<String, Integer> getArtistStats(int userId, int limit) {
        return topBy(userId, DatabaseHelper.COL_SONG_ARTIST, limit);
    }
    public Map<String, Integer> getMoodStats(int userId, int limit) {
        return topBy(userId, DatabaseHelper.COL_SONG_MOOD, limit);
    }

    private Map<String, Integer> topBy(int userId, String column, int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + column + ", COUNT(*) as cnt FROM " + DatabaseHelper.TABLE_SONGS +
                        " WHERE " + DatabaseHelper.COL_SONG_USER_ID + "=? AND " +
                        column + " IS NOT NULL AND " + column + " != '' " +
                        " GROUP BY " + column + " ORDER BY cnt DESC LIMIT " + limit,
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) map.put(c.getString(0), c.getInt(1));
        c.close();
        return map;
    }

    private Song cursorToSong(Cursor c) {
        Song s = new Song();
        s.setId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ID)));
        s.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_TITLE)));
        s.setArtist(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_ARTIST)));
        s.setGenre(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_GENRE)));
        s.setRating(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_RATING)));
        s.setMood(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_MOOD)));
        s.setComment(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_COMMENT)));
        s.setFavorite(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_FAVORITE)) == 1);
        s.setUserId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_USER_ID)));
        s.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_DATE)));
        s.setCoverPath(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SONG_COVER)));
        return s;
    }
}