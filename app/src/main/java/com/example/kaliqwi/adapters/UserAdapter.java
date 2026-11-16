package com.example.kaliqwi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaliqwi.R;
import com.example.kaliqwi.database.DatabaseHelper;
import com.example.kaliqwi.models.User;
import com.example.kaliqwi.utils.SessionManager;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnUserAction {
        void onUserDeleted();
        void onRoleChanged();
    }

    private final List<User> users;
    private final OnUserAction listener;
    private final DatabaseHelper db;

    public UserAdapter(List<User> users, OnUserAction listener, DatabaseHelper db) {
        this.users = users;
        this.listener = listener;
        this.db = db;
    }

    public void updateList(List<User> newList) {
        users.clear();
        users.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = users.get(position);
        h.tvName.setText(u.getUsername() + (u.isAdmin() ? " 👑" : ""));
        h.tvEmail.setText(u.getEmail());
        h.tvRole.setText(u.isAdmin() ? "Администратор" : "Пользователь");
        h.tvRole.setBackgroundResource(u.isAdmin()
                ? R.color.kaliqwi_accent : R.color.kaliqwi_primary);

        h.itemView.setOnClickListener(v -> showActions(u, h));
    }

    private void showActions(User user, VH h) {
        int currentUserId = new SessionManager(h.itemView.getContext()).getUserId();
        boolean isSelf = user.getId() == currentUserId;

        String[] actions;
        if (isSelf) {
            actions = new String[]{"Это вы"};
        } else if (user.isAdmin()) {
            actions = new String[]{"Снять роль администратора", "Удалить"};
        } else {
            actions = new String[]{"Сделать администратором", "Удалить"};
        }

        new AlertDialog.Builder(h.itemView.getContext())
                .setTitle(user.getUsername())
                .setItems(actions, (d, which) -> {
                    if (isSelf) return;

                    if (user.isAdmin()) {
                        if (which == 0) {
                            db.updateRole(user.getId(), DatabaseHelper.ROLE_USER);
                            listener.onRoleChanged();
                        } else if (which == 1) {
                            confirmDelete(user, h);
                        }
                    } else {
                        if (which == 0) {
                            db.updateRole(user.getId(), DatabaseHelper.ROLE_ADMIN);
                            listener.onRoleChanged();
                        } else if (which == 1) {
                            confirmDelete(user, h);
                        }
                    }
                })
                .show();
    }

    private void confirmDelete(User user, VH h) {
        new AlertDialog.Builder(h.itemView.getContext())
                .setTitle("Удалить пользователя?")
                .setMessage("Все песни «" + user.getUsername() + "» тоже удалятся.")
                .setPositiveButton("Удалить", (d, w) -> {
                    db.deleteUser(user.getId());
                    listener.onUserDeleted();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvRole = v.findViewById(R.id.tvRole);
        }
    }
}