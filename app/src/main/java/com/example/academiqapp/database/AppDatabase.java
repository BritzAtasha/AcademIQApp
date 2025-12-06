package com.example.academiqapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.academiqapp.models.Billing;
import com.example.academiqapp.models.Course;
import com.example.academiqapp.models.Grade;
import com.example.academiqapp.models.User;

@Database(entities = {User.class, Course.class, Grade.class, Billing.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract CourseDao courseDao();
    public abstract GradeDao gradeDao();
    public abstract BillingDao billingDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "academiq_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}