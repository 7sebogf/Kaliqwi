package com.example.kaliqwi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kaliqwi.R;
import com.example.kaliqwi.database.DatabaseHelper;
import com.example.kaliqwi.database.SongDao;
import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar, btnBack, btnEdit;
    private TextView tvUsername, tvEmail, tvRole;
    private TextView tvCountSongs, tvAvgRating, tvFavorites;
    private com.google.android.material.button.MaterialButton btnAdminPanel, btnDeleteAccount;

    private DatabaseHelper db;
    private SongDao songDao;
    private SessionManager session;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        songDao = new SongDao(this);
        session = new SessionManager(this);
        userId = session.getUserId();

        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvCountSongs = findViewById(R.id.tvCountSongs);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvFavorites = findViewById(R.id.tvFavorites);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
        btnDeleteAccount.setOnClickListener(v -> confirmDelete());
        btnAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));
    }

    @Override
    protected void onResume() { super.onResume(); loadProfile(); }

    private void loadProfile() {
        User user = db.getUserById(userId);
        if (user == null) { finish(); return; }

        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvRole.setText(user.isAdmin() ? "Администратор" : "Пользователь");
        tvRole.setBackgroundResource(user.isAdmin()
                ? R.color.kaliqwi_accent : R.color.kaliqwi_primary);

        String avatar = user.getAvatarPath();
        if (avatar != null && !avatar.isEmpty()) {
            try { imgAvatar.setImageURI(Uri.parse(avatar)); } catch (Exception ignored) {}
        }

        int count = songDao.getSongsByUser(userId).size();
        float avg = songDao.getAverageRating(userId);
        int favs = songDao.getFavorites(userId).size();

        tvCountSongs.setText(String.valueOf(count));
        tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
        tvFavorites.setText(String.valueOf(favs));

        btnAdminPanel.setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить аккаунт?")
                .setMessage("Все записи будут удалены навсегда. Продолжить?")
                .setPositiveButton("Удалить", (d, w) -> {
                    db.deleteUser(userId);
                    session.logout();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}