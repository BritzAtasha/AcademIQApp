package com.example.academiqapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academiqapp.R;
import com.example.academiqapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvSignUpLink, tvForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Skip login if already logged in
        if (sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
            navigateToMain();
            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Password recovery feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etLoginEmail.setError("Email is required");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Fetch username + admin flag from Firestore
                            db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            String username = doc.getString("username");
                                            Boolean isAdmin = doc.getBoolean("isAdmin");

                                            // 🔹 Save session atomically
                                            sessionManager.saveSession(
                                                    true,
                                                    userId,
                                                    username != null ? username : "User",
                                                    isAdmin != null && isAdmin
                                            );

                                            // Debug log
                                            Log.d("SESSION_DEBUG", "Logged in? " + sessionManager.isLoggedIn() +
                                                    ", Username: " + sessionManager.getUsername() +
                                                    ", UserId: " + sessionManager.getUserId() +
                                                    ", Admin? " + sessionManager.isAdmin());

                                            Toast.makeText(LoginActivity.this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                                            navigateToMain();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "User data missing in Firestore!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(LoginActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
