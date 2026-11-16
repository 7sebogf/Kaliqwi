package com.example.kaliqwi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoRegister, tvForgot;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) { goToMain(); return; }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Скоро добавим", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email)) { etEmail.setError("Введите email"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Введите пароль"); return; }

        int userId = db.loginUser(email, password);
        if (userId == -1) {
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = db.getUserById(userId);
        session.saveSession(userId, user.getUsername(), user.getRole(), user.getAvatarPath());
        Toast.makeText(this, "Добро пожаловать!", Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}