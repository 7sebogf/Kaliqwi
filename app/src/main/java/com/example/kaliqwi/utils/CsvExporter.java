package com.example.kaliqwi.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.kaliqwi.database.SongDao;
import com.example.kaliqwi.models.Song;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class CsvExporter {

    public static void exportAndShare(Context context, int userId) {
        try {
            List<Song> songs = new SongDao(context).getSongsByUser(userId);

            File dir = new File(context.getCacheDir(), "exports");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "kaliqwi_diary.csv");

            FileWriter fw = new FileWriter(file);
            fw.write("Title,Artist,Genre,Rating,Mood,Comment,Favorite,Date\n");
            for (Song s : songs) {
                fw.write(escape(s.getTitle()) + "," +
                        escape(s.getArtist()) + "," +
                        escape(s.getGenre()) + "," +
                        s.getRating() + "," +
                        escape(s.getMood()) + "," +
                        escape(s.getComment()) + "," +
                        (s.isFavorite() ? "yes" : "no") + "," +
                        escape(s.getDate()) + "\n");
            }
            fw.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_SUBJECT, "Kaliqwi — мой музыкальный дневник");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "Поделиться дневником"));
        } catch (Exception e) {
            Toast.makeText(context, "Ошибка экспорта: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}