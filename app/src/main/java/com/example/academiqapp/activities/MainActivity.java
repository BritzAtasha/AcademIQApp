package com.example.academiqapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.academiqapp.R;
import com.example.academiqapp.fragments.AboutFragment;
import com.example.academiqapp.fragments.BillingsFragment;
import com.example.academiqapp.fragments.CoursesFragment;
import com.example.academiqapp.fragments.DashboardFragment;
import com.example.academiqapp.fragments.GradesFragment;
import com.example.academiqapp.fragments.SettingsFragment;
import com.example.academiqapp.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView ivMenu;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // -------------------------------
        // 🔹 Check login state
        // -------------------------------
        if (!sessionManager.isLoggedIn() || sessionManager.getUserId() == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        // -------------------------------
        // 🔹 Initialize views
        // -------------------------------
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        ivMenu = findViewById(R.id.ivMenu);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // -------------------------------
        // 🔹 Navigation menu listener
        // -------------------------------
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
            } else if (id == R.id.nav_courses) {
                loadFragment(new CoursesFragment());
            } else if (id == R.id.nav_grades) {
                loadFragment(new GradesFragment());
            } else if (id == R.id.nav_billings) {
                loadFragment(new BillingsFragment());
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
            } else if (id == R.id.nav_about) {
                loadFragment(new AboutFragment());
            } else if (id == R.id.nav_logout) {
                sessionManager.logout();
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                finish();
                return true;
            } else {
                return false;
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // -------------------------------
        // 🔹 Navigation header
        // -------------------------------
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavUsername = headerView.findViewById(R.id.tvNavUsername);
        TextView tvNavUserType = headerView.findViewById(R.id.tvNavUserType);

        tvNavUsername.setText(sessionManager.getUsername() != null ? sessionManager.getUsername() : "User");
        tvNavUserType.setText(sessionManager.isAdmin() ? "Administrator" : "User");

        // -------------------------------
        // 🔹 Hamburger menu
        // -------------------------------
        ivMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // -------------------------------
        // 🔹 Default fragment
        // -------------------------------
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
