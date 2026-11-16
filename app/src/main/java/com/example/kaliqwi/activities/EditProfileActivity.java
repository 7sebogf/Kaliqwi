package com.example.kaliqwi.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.kaliqwi.R;
import com.example.kaliqwi.database.DatabaseHelper;
import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextInputEditText etUsername, etEmail, etNewPassword, etConfirmPassword;
    private MaterialButton btnSave;

    private DatabaseHelper db;
    private SessionManager session;
    private int userId;
    private String newAvatarPath = null;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, flags);
                    newAvatarPath = uri.toString();
                    imgAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId = session.getUserId();

        imgAvatar = findViewById(R.id.imgAvatar);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);

        User user = db.getUserById(userId);
        if (user != null) {
            etUsername.setText(user.getUsername());
            etEmail.setText(user.getEmail());
            if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                try { imgAvatar.setImageURI(Uri.parse(user.getAvatarPath())); }
                catch (Exception ignored) {}
            }
        }

        imgAvatar.setOnClickListener(v -> pickImage.launch("image/*"));
        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass1 = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
        String pass2 = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(username)) { etUsername.setError("Введите имя"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный email"); return;
        }
        if (db.isEmailTaken(email, userId)) { etEmail.setError("Email занят"); return; }

        db.updateProfile(userId, username, email, newAvatarPath);
        session.setUsername(username);
        if (newAvatarPath != null) session.setAvatar(newAvatarPath);

        if (!pass1.isEmpty() || !pass2.isEmpty()) {
            if (pass1.length() < 6) { etNewPassword.setError("Минимум 6 символов"); return; }
            if (!pass1.equals(pass2)) { etConfirmPassword.setError("Пароли не совпадают"); return; }
            db.updatePassword(userId, pass1);
            Toast.makeText(this, "Пароль изменён", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Профиль сохранён", Toast.LENGTH_SHORT).show();
        finish();
    }
}