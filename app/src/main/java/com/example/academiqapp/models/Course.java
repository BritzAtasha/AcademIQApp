package com.example.academiqapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Room Entity annotation
@Entity (tableName = "courses")
public class Course {

    // Room Primary Key: autoGenerate=true requires int or long
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String firestoreId; // Firestore document ID
    private String courseName;
    private String courseCode;
    private String instructor;
    private String description;

    // No-argument constructor required for Room and Firestore
    public Course() {
    }

    // Constructor with parameters (useful for creating new instances)
    public Course(String courseName, String courseCode, String instructor, String description) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.instructor = instructor;
        this.description = description;
    }

    // --- Getters and Setters ---

    // REQUIRED for Room to access the Primary Key
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}