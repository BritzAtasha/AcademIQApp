package com.example.academiqapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "billings")
public class Billing {

    @PrimaryKey
    @NonNull
    private String id = "";  // Firestore document ID

    private String category; // e.g., Tuition, Miscellaneous, Registration
    private double amount;   // Billing amount
    private String status;   // Paid, Pending, Overdue
    private String dueDate;  // Due date in String format (e.g., "2025-12-10")

    // Default constructor required for Firestore
    public Billing() {}

    public Billing(String category, double amount, String status, String dueDate) {
        this.category = category;
        this.amount = amount;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
