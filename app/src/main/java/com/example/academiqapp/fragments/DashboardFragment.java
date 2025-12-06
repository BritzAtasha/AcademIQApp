package com.example.academiqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.academiqapp.R;
import com.example.academiqapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvDashboardUsername;
    private TextView tvGWA, tvUnits, tvBalance, tvAttendance;

    private TextView tvSchedule1Code, tvSchedule1Time;
    private TextView tvSchedule2Code, tvSchedule2Time;
    private TextView tvSchedule3Code, tvSchedule3Time;

    private CardView cvGWA, cvUnits, cvBalance, cvAttendance;

    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initializeViews(view);

        sessionManager = new SessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        // Set username safely
        tvDashboardUsername.setText(sessionManager.getUsername() != null ? sessionManager.getUsername() : "User");

        // Load stats and schedule safely
        loadUserStatistics();
        loadTodaySchedule();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvDashboardUsername = view.findViewById(R.id.tvDashboardUsername);
        tvGWA = view.findViewById(R.id.tvGWA);
        tvUnits = view.findViewById(R.id.tvUnits);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvAttendance = view.findViewById(R.id.tvAttendance);

        tvSchedule1Code = view.findViewById(R.id.tvSchedule1Code);
        tvSchedule1Time = view.findViewById(R.id.tvSchedule1Time);
        tvSchedule2Code = view.findViewById(R.id.tvSchedule2Code);
        tvSchedule2Time = view.findViewById(R.id.tvSchedule2Time);
        tvSchedule3Code = view.findViewById(R.id.tvSchedule3Code);
        tvSchedule3Time = view.findViewById(R.id.tvSchedule3Time);
    }

    private void loadUserStatistics() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            setDefaultValues();
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        updateStatCard(tvGWA, document.getDouble("gwa"), "%.1f", "2.0");
                        updateStatCard(tvUnits, document.getLong("units"), "%d", "0");
                        updateStatCard(tvBalance, document.getDouble("balance"), "₱%.0f", "₱0");
                        updateStatCard(tvAttendance, document.getDouble("attendance"), "%.0f%%", "0%");
                    } else {
                        setDefaultValues();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load statistics", Toast.LENGTH_SHORT).show();
                    setDefaultValues();
                });
    }

    private void updateStatCard(TextView textView, Number value, String format, String defaultValue) {
        textView.setText(value != null ? String.format(Locale.getDefault(), format, value) : defaultValue);
    }

    private void setDefaultValues() {
        tvGWA.setText("2.0");
        tvUnits.setText("0");
        tvBalance.setText("₱0");
        tvAttendance.setText("0%");
    }

    private void loadTodaySchedule() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().getTime());

        db.collection("schedules")
                .whereEqualTo("userId", userId)
                .whereEqualTo("day", today)
                .orderBy("startTime")
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Schedule> schedules = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        schedules.add(new Schedule(
                                doc.getString("courseCode"),
                                doc.getString("startTime"),
                                doc.getString("endTime")
                        ));
                    }

                    // Safely update schedule TextViews
                    for (int i = 0; i < schedules.size(); i++) {
                        Schedule s = schedules.get(i);
                        if (i == 0) {
                            tvSchedule1Code.setText(s.courseCode);
                            tvSchedule1Time.setText(s.startTime + " - " + s.endTime);
                        } else if (i == 1) {
                            tvSchedule2Code.setText(s.courseCode);
                            tvSchedule2Time.setText(s.startTime + " - " + s.endTime);
                        } else if (i == 2) {
                            tvSchedule3Code.setText(s.courseCode);
                            tvSchedule3Time.setText(s.startTime + " - " + s.endTime);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load schedule", Toast.LENGTH_SHORT).show());
    }

    private void setupClickListeners() {
        // Optional: Add click listeners for your stat cards
    }

    private static class Schedule {
        String courseCode, startTime, endTime;

        Schedule(String courseCode, String startTime, String endTime) {
            this.courseCode = courseCode;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
