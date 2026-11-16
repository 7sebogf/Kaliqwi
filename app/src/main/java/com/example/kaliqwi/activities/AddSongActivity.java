package com.example.kaliqwi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.kaliqwi.R;
import com.example.kaliqwi.database.SongDao;
import com.example.kaliqwi.models.Song;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AddSongActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etArtist, etComment;
    private AutoCompleteTextView etGenre, etMood;
    private RatingBar ratingBar;
    private MaterialButton btnSave;
    private ImageView imgCover;
    private TextView tvScreenTitle;

    private SongDao songDao;
    private SessionManager session;

    private int editSongId = -1;
    private String coverPath = null;

    private static final String[] GENRES = {
            "Pop", "Rock", "Hip-Hop", "Rap", "Jazz", "Classical",
            "Electronic", "Indie", "Metal", "R&B", "Reggae", "Другое"
    };
    private static final String[] MOODS = {
            "Радость", "Грусть", "Спокойствие", "Энергия",
            "Ностальгия", "Романтика", "Меланхолия", "Другое"
    };

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    coverPath = uri.toString();
                    imgCover.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        songDao = new SongDao(this);
        session = new SessionManager(this);

        etTitle = findViewById(R.id.etTitle);
        etArtist = findViewById(R.id.etArtist);
        etGenre = findViewById(R.id.etGenre);
        etMood = findViewById(R.id.etMood);
        etComment = findViewById(R.id.etComment);
        ratingBar = findViewById(R.id.ratingBar);
        btnSave = findViewById(R.id.btnSave);
        imgCover = findViewById(R.id.imgCover);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);

        etGenre.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, GENRES));
        etMood.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, MOODS));

        editSongId = getIntent().getIntExtra("song_id", -1);
        if (editSongId != -1) {
            if (tvScreenTitle != null) tvScreenTitle.setText("Редактирование");
            btnSave.setText("Сохранить изменения");
            Song s = songDao.getSongById(editSongId);
            if (s != null) fillForm(s);
        }

        imgCover.setOnClickListener(v -> pickImage.launch("image/*"));
        btnSave.setOnClickListener(v -> saveSong());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void fillForm(Song s) {
        etTitle.setText(s.getTitle());
        etArtist.setText(s.getArtist());
        etGenre.setText(s.getGenre(), false);
        etMood.setText(s.getMood(), false);
        etComment.setText(s.getComment());
        ratingBar.setRating(s.getRating());
        coverPath = s.getCoverPath();
        if (coverPath != null && !coverPath.isEmpty()) {
            try { imgCover.setImageURI(Uri.parse(coverPath)); } catch (Exception ignored) {}
        }
    }

    private void saveSong() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String artist = etArtist.getText() != null ? etArtist.getText().toString().trim() : "";
        String genre = etGenre.getText() != null ? etGenre.getText().toString().trim() : "";
        String mood = etMood.getText() != null ? etMood.getText().toString().trim() : "";
        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        int rating = (int) ratingBar.getRating();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Введите название");
            etTitle.requestFocus();
            return;
        }

        int currentUserId = session.getUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Ошибка: не залогинен", Toast.LENGTH_SHORT).show();
            return;
        }

        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setGenre(genre);
        song.setMood(mood);
        song.setComment(comment);
        song.setRating(rating);
        song.setUserId(currentUserId);
        song.setCoverPath(coverPath);

        if (editSongId == -1) {
            song.setFavorite(false);
            song.setDate(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date()));
            long id = songDao.addSong(song);
            if (id == -1) {
                Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Добавлено 🎵", Toast.LENGTH_SHORT).show();
        } else {
            Song old = songDao.getSongById(editSongId);
            song.setId(editSongId);
            song.setFavorite(old != null && old.isFavorite());
            song.setDate(old != null ? old.getDate() : "");
            songDao.updateSong(song);
            Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}