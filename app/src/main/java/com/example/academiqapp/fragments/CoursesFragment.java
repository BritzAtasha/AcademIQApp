package com.example.academiqapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academiqapp.R;
import com.example.academiqapp.adapters.CourseAdapter;
import com.example.academiqapp.models.Course;
import com.example.academiqapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CoursesFragment extends Fragment implements CourseAdapter.OnCourseActionListener {

    private RecyclerView recyclerViewCourses;
    private TextView tvEmptyCourses;
    private FloatingActionButton fabAddCourse;
    private CourseAdapter courseAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private List<Course> courseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        recyclerViewCourses = view.findViewById(R.id.recyclerViewCourses);
        tvEmptyCourses = view.findViewById(R.id.tvEmptyCourses);
        fabAddCourse = view.findViewById(R.id.fabAddCourse);

        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(getContext()));

        if (sessionManager.isAdmin()) {
            fabAddCourse.setVisibility(View.VISIBLE);
            fabAddCourse.setOnClickListener(v -> showAddCourseDialog(null));
        }

        loadCourses();
        return view;
    }

    private void loadCourses() {
        db.collection("courses").get().addOnSuccessListener(queryDocumentSnapshots -> {
            courseList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Course course = doc.toObject(Course.class);
                if (course != null) {
                    course.setFirestoreId(doc.getId());
                    courseList.add(course);
                }
            }

            if (courseList.isEmpty()) {
                tvEmptyCourses.setVisibility(View.VISIBLE);
                recyclerViewCourses.setVisibility(View.GONE);
            } else {
                tvEmptyCourses.setVisibility(View.GONE);
                recyclerViewCourses.setVisibility(View.VISIBLE);
                courseAdapter = new CourseAdapter(courseList, sessionManager.isAdmin(), this);
                recyclerViewCourses.setAdapter(courseAdapter);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error loading courses: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showAddCourseDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_course, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etDialogCourseName = dialogView.findViewById(R.id.etDialogCourseName);
        EditText etDialogCourseCode = dialogView.findViewById(R.id.etDialogCourseCode);
        EditText etDialogInstructor = dialogView.findViewById(R.id.etDialogInstructor);
        EditText etDialogDescription = dialogView.findViewById(R.id.etDialogDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        tvDialogTitle.setText(course == null ? "Add Course" : "Edit Course");

        if (course != null) {
            etDialogCourseName.setText(course.getCourseName());
            etDialogCourseCode.setText(course.getCourseCode());
            etDialogInstructor.setText(course.getInstructor());
            etDialogDescription.setText(course.getDescription());
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String courseName = etDialogCourseName.getText().toString().trim();
            String courseCode = etDialogCourseCode.getText().toString().trim();
            String instructor = etDialogInstructor.getText().toString().trim();
            String description = etDialogDescription.getText().toString().trim();

            if (courseName.isEmpty()) { etDialogCourseName.setError("Required"); return; }
            if (courseCode.isEmpty()) { etDialogCourseCode.setError("Required"); return; }
            if (instructor.isEmpty()) { etDialogInstructor.setError("Required"); return; }

            if (course == null) {
                Course newCourse = new Course(courseName, courseCode, instructor, description);
                db.collection("courses").add(newCourse).addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Course added", Toast.LENGTH_SHORT).show();
                    loadCourses();
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                course.setCourseName(courseName);
                course.setCourseCode(courseCode);
                course.setInstructor(instructor);
                course.setDescription(description);

                db.collection("courses").document(course.getFirestoreId())
                        .set(course)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Course updated", Toast.LENGTH_SHORT).show();
                            loadCourses();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onEditClick(Course course) {
        showAddCourseDialog(course);
    }

    @Override
    public void onDeleteClick(Course course) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) ->
                        db.collection("courses").document(course.getFirestoreId())
                                .delete()
                                .addOnSuccessListener(aVoid -> loadCourses())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
