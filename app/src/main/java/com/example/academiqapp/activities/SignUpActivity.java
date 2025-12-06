package com.example.academiqapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academiqapp.R;
import com.example.academiqapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private CheckBox cbAdmin;
    private Button btnCreateAccount;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbAdmin = findViewById(R.id.cbAdmin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        btnCreateAccount.setOnClickListener(v -> signUpUser());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void signUpUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isAdmin = cbAdmin.isChecked();

        if (username.isEmpty()) { etUsername.setError("Username is required"); etUsername.requestFocus(); return; }
        if (email.isEmpty()) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (password.isEmpty()) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (password.length() < 6) { etPassword.setError("Password must be at least 6 characters"); etPassword.requestFocus(); return; }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("isAdmin", isAdmin);

                            db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // 🔹 Save session atomically
                                        sessionManager.saveSession(
                                                true,
                                                userId,
                                                username,
                                                isAdmin
                                        );

                                        Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignUpActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
