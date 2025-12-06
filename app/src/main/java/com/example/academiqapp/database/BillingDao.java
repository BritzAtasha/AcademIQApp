package com.example.academiqapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.academiqapp.models.Billing;

import java.util.List;

@Dao
public interface BillingDao {
    @Insert
    void insert(Billing billing);

    @Update
    void update(Billing billing);

    @Delete
    void delete(Billing billing);

    @Query("SELECT * FROM billings")
    List<Billing> getAllBillings();

    @Query("SELECT * FROM billings WHERE id = :id")
    Billing getBillingById(int id);

    @Query("SELECT SUM(amount) FROM billings WHERE status != 'Paid'")
    Double getTotalDue();

    @Query("SELECT SUM(amount) FROM billings")
    Double getTotalAmount();
}