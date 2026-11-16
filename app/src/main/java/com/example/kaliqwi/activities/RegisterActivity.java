package com.example.kaliqwi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.kaliqwi.R;
import com.example.kaliqwi.database.DatabaseHelper;
import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.SessionManager;
import com.example.kaliqwi.utils.ThemeHelper;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirm;
    private MaterialButton btnRegister;
    private TextView tvGoLogin;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

        if (TextUtils.isEmpty(username)) { etUsername.setError("Введите имя"); return; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Некорректный email"); return;
        }
        if (password.length() < 6) { etPassword.setError("Минимум 6 символов"); return; }
        if (!password.equals(confirm)) { etConfirm.setError("Пароли не совпадают"); return; }

        if (!db.registerUser(username, email, password)) {
            Toast.makeText(this, "Email уже зарегистрирован", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = db.loginUser(email, password);
        User user = db.getUserById(userId);
        session.saveSession(userId, user.getUsername(), user.getRole(), user.getAvatarPath());

        Toast.makeText(this, "Аккаунт создан!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}