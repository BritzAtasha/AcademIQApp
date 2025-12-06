package com.example.academiqapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.academiqapp.R;
import com.example.academiqapp.activities.WelcomeActivity;
import com.example.academiqapp.database.AppDatabase;
import com.example.academiqapp.models.User;
import com.example.academiqapp.utils.SessionManager;

public class SettingsFragment extends Fragment {

    private TextView tvSettingsUsername, tvSettingsUserType;
    private Button btnChangePassword, btnDeleteAccount;
    private SessionManager sessionManager;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sessionManager = new SessionManager(getContext());
        database = AppDatabase.getInstance(getContext());

        tvSettingsUsername = view.findViewById(R.id.tvSettingsUsername);
        tvSettingsUserType = view.findViewById(R.id.tvSettingsUserType);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        // Safe user info
        String username = sessionManager.getUsername() != null ? sessionManager.getUsername() : "User";
        String userType = sessionManager.isAdmin() ? "Administrator" : "User";
        tvSettingsUsername.setText("Username: " + username);
        tvSettingsUserType.setText("Account Type: " + userType);

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        return view;
    }

    private void showChangePasswordDialog() {
        String userIdStr = sessionManager.getUserId();
        if (userIdStr == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid User ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                User user = database.userDao().getUserById(userId);
                if (user != null && user.getPassword().equals(oldPassword)) {
                    user.setPassword(newPassword);
                    database.userDao().update(user);
                    Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Incorrect old password", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteAccountDialog() {
        String userIdStr = sessionManager.getUserId();
        if (userIdStr == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid User ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        User user = database.userDao().getUserById(userId);
                        if (user != null) {
                            database.userDao().delete(user);
                            sessionManager.logout();
                            Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getContext(), WelcomeActivity.class));
                            getActivity().finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
