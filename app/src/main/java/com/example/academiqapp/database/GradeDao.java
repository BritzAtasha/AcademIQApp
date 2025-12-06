package com.example.academiqapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.academiqapp.models.Grade;

import java.util.List;

@Dao
public interface GradeDao {
    @Insert
    void insert(Grade grade);

    @Update
    void update(Grade grade);

    @Delete
    void delete(Grade grade);

    @Query("SELECT * FROM grades")
    List<Grade> getAllGrades();

    @Query("SELECT * FROM grades WHERE id = :id")
    Grade getGradeById(int id);
}