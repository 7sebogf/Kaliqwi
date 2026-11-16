package com.example.kaliqwi.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaliqwi.R;
import com.example.kaliqwi.adapters.UserAdapter;
import com.example.kaliqwi.database.DatabaseHelper;
import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.ThemeHelper;

import java.util.List;

public class AdminActivity extends AppCompatActivity implements UserAdapter.OnUserAction {

    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    @Override
    protected void onResume() { super.onResume(); loadUsers(); }

    private void loadUsers() {
        List<User> users = db.getAllUsers();
        if (adapter == null) {
            adapter = new UserAdapter(users, this, db);
            recyclerUsers.setAdapter(adapter);
        } else {
            adapter.updateList(users);
        }
    }

    @Override
    public void onUserDeleted() { loadUsers(); }
    @Override
    public void onRoleChanged() { loadUsers(); }
}