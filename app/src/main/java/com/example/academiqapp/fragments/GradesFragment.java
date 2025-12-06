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
import com.example.academiqapp.adapters.GradeAdapter;
import com.example.academiqapp.models.Grade;
import com.example.academiqapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GradesFragment extends Fragment {

    private RecyclerView recyclerViewGrades;
    private TextView tvGpa, tvUnits, tvNoGrades;
    private FloatingActionButton fabAddGrade;
    private GradeAdapter gradesAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    private List<Grade> gradeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grades, container, false);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        recyclerViewGrades = view.findViewById(R.id.recyclerViewGrades);
        tvGpa = view.findViewById(R.id.tvGpa);
        tvUnits = view.findViewById(R.id.tvUnits);
        tvNoGrades = view.findViewById(R.id.tvNoGrades);
        fabAddGrade = view.findViewById(R.id.fabAddGrade);

        recyclerViewGrades.setLayoutManager(new LinearLayoutManager(getContext()));

        // Only show Add Grade FAB if admin
        if (sessionManager.isAdmin()) {
            fabAddGrade.setVisibility(View.VISIBLE);
            fabAddGrade.setOnClickListener(v -> showAddGradeDialog(null));
        } else {
            fabAddGrade.setVisibility(View.GONE);
        }

        loadGrades();

        return view;
    }

    private void loadGrades() {
        db.collection("grades")
                .get()
                .addOnSuccessListener(query -> {
                    gradeList = new ArrayList<>();

                    for (DocumentSnapshot doc : query) {
                        Grade grade = doc.toObject(Grade.class);
                        if (grade != null) {
                            grade.setFirestoreId(doc.getId());
                            gradeList.add(grade);
                        }
                    }

                    if (gradeList.isEmpty()) {
                        tvNoGrades.setVisibility(View.VISIBLE);
                        recyclerViewGrades.setVisibility(View.GONE);
                        tvGpa.setText("GPA: 0.00");
                        tvUnits.setText("Units: 0");
                    } else {
                        tvNoGrades.setVisibility(View.GONE);
                        recyclerViewGrades.setVisibility(View.VISIBLE);

                        gradesAdapter = new GradeAdapter(gradeList, sessionManager.isAdmin(), new GradeAdapter.OnGradeActionListener() {
                            @Override
                            public void onEditClick(Grade grade) {
                                showAddGradeDialog(grade);
                            }

                            @Override
                            public void onDeleteClick(Grade grade) {
                                deleteGrade(grade);
                            }
                        });

                        recyclerViewGrades.setAdapter(gradesAdapter);

                        calculateGpa();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading grades: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateGpa() {
        double totalUnits = 0;
        double weightedPoints = 0;

        for (Grade grade : gradeList) {
            int units = grade.getUnits();
            double gradeVal = grade.getGradeValue();
            double gradePoint = convertGradeToPoint(gradeVal);

            weightedPoints += gradePoint * units;
            totalUnits += units;
        }

        double gpa = (totalUnits == 0) ? 0 : weightedPoints / totalUnits;

        tvGpa.setText("GPA: " + String.format("%.2f", gpa));
        tvUnits.setText("Units: " + (int) totalUnits);
    }

    private double convertGradeToPoint(double gradeValue) {
        if (gradeValue >= 96) return 4.00;
        if (gradeValue >= 90) return 3.50;
        if (gradeValue >= 85) return 3.00;
        if (gradeValue >= 80) return 2.50;
        if (gradeValue >= 75) return 2.00;
        return 1.00;
    }

    private void showAddGradeDialog(Grade grade) {
        // Safety check: only admin can add/edit
        if (!sessionManager.isAdmin()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_grade, null);

        TextView tvDialogGradeTitle = dialogView.findViewById(R.id.tvDialogGradeTitle);
        EditText etCourseCode = dialogView.findViewById(R.id.etDialogGradeCourseCode);
        EditText etCourseName = dialogView.findViewById(R.id.etDialogGradeCourseName);
        EditText etGradeValue = dialogView.findViewById(R.id.etDialogGradeValue);
        EditText etUnits = dialogView.findViewById(R.id.etDialogGradeUnits);
        Button btnCancel = dialogView.findViewById(R.id.btnGradeCancel);
        Button btnSave = dialogView.findViewById(R.id.btnGradeSave);

        if (grade != null) {
            tvDialogGradeTitle.setText("Edit Grade");
            etCourseCode.setText(grade.getCourseCode());
            etCourseName.setText(grade.getCourseName());
            etGradeValue.setText(String.valueOf(grade.getGradeValue()));
            etUnits.setText(String.valueOf(grade.getUnits()));
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String courseCode = etCourseCode.getText().toString().trim();
            String courseName = etCourseName.getText().toString().trim();
            String gradeValStr = etGradeValue.getText().toString().trim();
            String unitsStr = etUnits.getText().toString().trim();

            if (courseCode.isEmpty()) { etCourseCode.setError("Required"); return; }
            if (courseName.isEmpty()) { etCourseName.setError("Required"); return; }
            if (gradeValStr.isEmpty()) { etGradeValue.setError("Required"); return; }
            if (unitsStr.isEmpty()) { etUnits.setError("Required"); return; }

            double gradeValue, units;
            try {
                gradeValue = Double.parseDouble(gradeValStr);
                units = Double.parseDouble(unitsStr);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                return;
            }

            if (grade == null) {
                Grade newGrade = new Grade();
                newGrade.setCourseCode(courseCode);
                newGrade.setCourseName(courseName);
                newGrade.setGradeValue(gradeValue);
                newGrade.setUnits((int) units);

                db.collection("grades")
                        .add(newGrade)
                        .addOnSuccessListener(docRef -> {
                            newGrade.setFirestoreId(docRef.getId());
                            Toast.makeText(getContext(), "Grade added successfully!", Toast.LENGTH_SHORT).show();
                            loadGrades();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                grade.setCourseCode(courseCode);
                grade.setCourseName(courseName);
                grade.setGradeValue(gradeValue);
                grade.setUnits((int) units);

                db.collection("grades").document(grade.getFirestoreId())
                        .set(grade)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Grade updated successfully!", Toast.LENGTH_SHORT).show();
                            loadGrades();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteGrade(Grade grade) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Grade")
                .setMessage("Are you sure you want to delete this grade?")
                .setPositiveButton("Delete", (dialog, which) ->
                        db.collection("grades").document(grade.getFirestoreId())
                                .delete()
                                .addOnSuccessListener(aVoid -> loadGrades())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                )
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}
