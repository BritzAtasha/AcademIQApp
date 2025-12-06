package com.example.academiqapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "grades")
public class Grade {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String firestoreId;
    private int studentId;
    private int courseId;
    private double gradeValue;
    private String semester;
    private int units;
    private String courseCode;
    private String courseName;

    // No-argument constructor (required for Firestore)
    public Grade() { }

    // Constructor with parameters
    public Grade(int studentId, int courseId, double gradeValue, String semester, int units) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.gradeValue = gradeValue;
        this.semester = semester;
        this.units = units;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public double getGradeValue() { return gradeValue; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
