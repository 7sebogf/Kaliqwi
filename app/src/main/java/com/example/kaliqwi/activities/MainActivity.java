package com.example.kaliqwi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.example.kaliqwi.R;
import com.example.kaliqwi.adapters.SongAdapter;
import com.example.kaliqwi.database.SongDao;
import com.example.kaliqwi.models.Song;
import com.example.kaliqwi.utils.CsvExporter;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnSongActionListener {

    private RecyclerView recyclerSongs;
    private SongAdapter adapter;
    private final List<Song> songList = new ArrayList<>();
    private TextView tvHello, tvStats, tvEmptyText;
    private LinearLayout tvEmpty;
    private EditText etSearch;
    private ImageView imgProfileTop;
    private Chip chipAll, chipFavorites, chipStats, chipExport;

    private SongDao songDao;
    private SessionManager session;
    private int userId;
    private boolean onlyFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        songDao = new SongDao(this);
        userId = session.getUserId();
        android.util.Log.d("KALIQWI", "MainActivity.onCreate userId=" + userId);

        tvHello = findViewById(R.id.tvHello);
        tvStats = findViewById(R.id.tvStats);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        etSearch = findViewById(R.id.etSearch);
        recyclerSongs = findViewById(R.id.recyclerSongs);
        imgProfileTop = findViewById(R.id.imgProfileTop);
        chipAll = findViewById(R.id.chipAll);
        chipFavorites = findViewById(R.id.chipFavorites);
        chipStats = findViewById(R.id.chipStats);
        chipExport = findViewById(R.id.chipExport);

        tvHello.setText(getString(R.string.hello_user, session.getUsername()));

        adapter = new SongAdapter(songList, this);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        recyclerSongs.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddSongActivity.class)));

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.btnTheme).setOnClickListener(v -> {
            android.util.Log.d("THEME", "Кнопка темы нажата!");
            ThemeHelper.toggle(this);
            recreate();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        if (chipAll != null) chipAll.setOnClickListener(v -> { onlyFavorites = false; loadSongs(); });
        if (chipFavorites != null) chipFavorites.setOnClickListener(v -> { onlyFavorites = true; loadSongs(); });
        if (chipStats != null) chipStats.setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
        if (chipExport != null) chipExport.setOnClickListener(v ->
                CsvExporter.exportAndShare(this, userId));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { loadSongs(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userId = session.getUserId();   // ★ на случай смены пользователя
        refreshAvatar();
        loadSongs();
    }

    private void refreshAvatar() {
        String avatar = session.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try { imgProfileTop.setImageURI(Uri.parse(avatar)); }
            catch (Exception ignored) {}
        }
    }

    private void loadSongs() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        List<Song> loaded;
        if (!query.isEmpty()) {
            loaded = songDao.search(userId, query);
        } else if (onlyFavorites) {
            loaded = songDao.getFavorites(userId);
        } else {
            loaded = songDao.getSongsByUser(userId);
        }
        songList.clear();
        songList.addAll(loaded);
        adapter.notifyDataSetChanged();
        android.util.Log.d("KALIQWI", "loadSongs userId=" + userId
                + " onlyFav=" + onlyFavorites + " query=" + query
                + " count=" + songList.size());
        updateUI();
    }

    private void updateUI() {
        int count = songList.size();
        float avg = songDao.getAverageRating(userId);
        tvStats.setText(String.format(Locale.getDefault(),
                "%d записей · средняя %.1f", count, avg));

        boolean empty = count == 0;
        tvEmpty.setVisibility(empty ? LinearLayout.VISIBLE : LinearLayout.GONE);
        recyclerSongs.setVisibility(empty ? RecyclerView.GONE : RecyclerView.VISIBLE);

        if (empty) {
            tvEmptyText.setText(onlyFavorites
                    ? "В избранном пока пусто ⭐"
                    : "Пока пусто.\nНажми + чтобы добавить первую песню");
        }
    }

    @Override
    public void onFavoriteClick(Song song) {
        boolean newFav = !song.isFavorite();
        songDao.toggleFavorite(song.getId(), newFav);
        song.setFavorite(newFav);
        if (onlyFavorites) loadSongs(); else adapter.notifyDataSetChanged();
        Toast.makeText(this, newFav ? "⭐ В избранном" : "Убрано",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Song song) {
        Intent i = new Intent(this, AddSongActivity.class);
        i.putExtra("song_id", song.getId());
        startActivity(i);
    }

    @Override
    public void onItemLongClick(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить?")
                .setMessage("Удалить «" + song.getTitle() + "»?")
                .setPositiveButton("Удалить", (d, w) -> {
                    songDao.deleteSong(song.getId());
                    loadSongs();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}