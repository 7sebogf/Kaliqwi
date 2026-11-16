package com.example.kaliqwi.models;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private int rating;
    private String mood;
    private String comment;
    private boolean favorite;
    private int userId;
    private String date;
    private String coverPath;   // NEW — путь к обложке

    public Song() {}

    public Song(int id, String title, String artist, String genre,
                int rating, String mood, String comment,
                boolean favorite, int userId, String date, String coverPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.rating = rating;
        this.mood = mood;
        this.comment = comment;
        this.favorite = favorite;
        this.userId = userId;
        this.date = date;
        this.coverPath = coverPath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getGenre() { return genre; }
    public int getRating() { return rating; }
    public String getMood() { return mood; }
    public String getComment() { return comment; }
    public boolean isFavorite() { return favorite; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
    public String getCoverPath() { return coverPath; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setRating(int rating) { this.rating = rating; }
    public void setMood(String mood) { this.mood = mood; }
    public void setComment(String comment) { this.comment = comment; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
}