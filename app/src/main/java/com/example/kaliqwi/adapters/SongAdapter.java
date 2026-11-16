package com.example.kaliqwi.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaliqwi.R;
import com.example.kaliqwi.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public interface OnSongActionListener {
        void onFavoriteClick(Song song);
        void onItemClick(Song song);
        void onItemLongClick(Song song);
    }

    private final List<Song> songs;
    private final OnSongActionListener listener;

    public SongAdapter(List<Song> songs, OnSongActionListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder h, int position) {
        Song s = songs.get(position);

        h.tvTitle.setText(s.getTitle());
        h.tvArtist.setText(s.getArtist() != null && !s.getArtist().isEmpty()
                ? s.getArtist() : "—");
        h.tvGenre.setText(s.getGenre() != null && !s.getGenre().isEmpty()
                ? s.getGenre() : "—");
        h.tvMood.setText(s.getMood() != null && !s.getMood().isEmpty()
                ? s.getMood() : "—");
        h.tvComment.setText(s.getComment() != null && !s.getComment().isEmpty()
                ? s.getComment() : "");
        h.tvDate.setText(s.getDate() != null ? s.getDate() : "");
        h.tvRating.setText(String.valueOf(s.getRating()));

        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            try {
                h.imgCover.setImageURI(Uri.parse(s.getCoverPath()));
            } catch (Exception e) {
                h.imgCover.setImageResource(android.R.drawable.ic_media_play);
            }
        } else {
            h.imgCover.setImageResource(android.R.drawable.ic_media_play);
        }

        h.btnFavorite.setImageResource(s.isFavorite()
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        h.btnFavorite.setOnClickListener(v -> listener.onFavoriteClick(s));
        h.itemView.setOnClickListener(v -> listener.onItemClick(s));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(s);
            return true;
        });
    }

    @Override
    public int getItemCount() { return songs.size(); }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover, btnFavorite;
        TextView tvTitle, tvArtist, tvGenre, tvMood, tvComment, tvDate, tvRating;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

}