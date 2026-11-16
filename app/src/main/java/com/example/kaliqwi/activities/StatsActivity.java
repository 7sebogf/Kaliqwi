package com.example.kaliqwi.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kaliqwi.R;
import com.example.kaliqwi.database.SongDao;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

import java.util.Locale;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private LinearLayout containerGenres, containerArtists, containerMoods;
    private TextView tvTotalSongs, tvAvg, tvFavs;
    private SongDao songDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        songDao = new SongDao(this);
        userId = new SessionManager(this).getUserId();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        containerGenres = findViewById(R.id.containerGenres);
        containerArtists = findViewById(R.id.containerArtists);
        containerMoods = findViewById(R.id.containerMoods);
        tvTotalSongs = findViewById(R.id.tvTotalSongs);
        tvAvg = findViewById(R.id.tvAvg);
        tvFavs = findViewById(R.id.tvFavs);

        loadStats();
    }

    private void loadStats() {
        int total = songDao.getSongsByUser(userId).size();
        float avg = songDao.getAverageRating(userId);
        int favs = songDao.getFavorites(userId).size();

        tvTotalSongs.setText(String.valueOf(total));
        tvAvg.setText(String.format(Locale.getDefault(), "%.1f", avg));
        tvFavs.setText(String.valueOf(favs));

        fill(containerGenres, songDao.getGenreStats(userId, 5));
        fill(containerArtists, songDao.getArtistStats(userId, 5));
        fill(containerMoods, songDao.getMoodStats(userId, 5));
    }

    private void fill(LinearLayout container, Map<String, Integer> data) {
        container.removeAllViews();
        if (data.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Нет данных");
            tv.setTextColor(getResources().getColor(R.color.text_secondary));
            container.addView(tv);
            return;
        }

        int max = 1;
        for (int v : data.values()) max = Math.max(max, v);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            View row = inflater.inflate(R.layout.item_stat_row, container, false);
            ((TextView) row.findViewById(R.id.tvLabel)).setText(e.getKey());
            ((TextView) row.findViewById(R.id.tvValue)).setText(String.valueOf(e.getValue()));
            ProgressBar pb = row.findViewById(R.id.progressBar);
            pb.setMax(max);
            pb.setProgress(e.getValue());
            container.addView(row);
        }
    }
}