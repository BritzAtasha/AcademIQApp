package com.example.academiqapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academiqapp.R;
import com.example.academiqapp.models.Course;
import com.example.academiqapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourseDetailActivity extends AppCompatActivity {

    private TextView tvCourseName, tvCourseCode, tvInstructor, tvDescription;
    private Button btnEdit, btnDelete;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String courseId;
    private Course currentCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_course);

        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseCode = findViewById(R.id.tvCourseCode);
        tvInstructor = findViewById(R.id.tvInstructor);
        tvDescription = findViewById(R.id.tvDescription);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        courseId = getIntent().getStringExtra("courseId");

        if (!sessionManager.isAdmin()) {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        loadCourseDetails();

        btnEdit.setOnClickListener(v -> showEditCourseDialog());
        btnDelete.setOnClickListener(v -> confirmDeleteCourse());
    }

    private void loadCourseDetails() {
        if (courseId == null) return;

        db.collection("courses").document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentCourse = documentSnapshot.toObject(Course.class);
                        if (currentCourse != null) {
                            tvCourseName.setText(currentCourse.getCourseName());
                            tvCourseCode.setText(currentCourse.getCourseCode());
                            tvInstructor.setText("Instructor: " + currentCourse.getInstructor());
                            tvDescription.setText(currentCourse.getDescription());
                        }
                    } else {
                        Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading course: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditCourseDialog() {
        // Implement edit dialog similar to your fragment
    }

    private void confirmDeleteCourse() {
        if (currentCourse == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("courses").document(courseId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error deleting course: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
