package com.example.academiqapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academiqapp.R;
import com.example.academiqapp.utils.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnSignUp;
    private TextView tvLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // -------------------------------
        // 🔹 Auto-navigate logged-in users
        // -------------------------------
        if (sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignUp.setOnClickListener(v -> startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class)));
        tvLogin.setOnClickListener(v -> startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));
    }
}
