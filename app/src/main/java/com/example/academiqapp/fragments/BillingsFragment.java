package com.example.academiqapp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView; // <-- NEW REQUIRED IMPORT

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academiqapp.R;
import com.example.academiqapp.adapters.BillingAdapter;
import com.example.academiqapp.models.Billing;
import com.example.academiqapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BillingsFragment extends Fragment implements BillingAdapter.OnBillingActionListener {

    private RecyclerView recyclerViewBillings;
    private TextView tvEmptyBillings, tvTotalDue, tvNextDue;
    private FloatingActionButton fabAddBilling;
    private BillingAdapter billingAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private List<Billing> billingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_billings, container, false);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        recyclerViewBillings = view.findViewById(R.id.recyclerViewBillings);
        tvEmptyBillings = view.findViewById(R.id.tvEmptyBillings);
        tvTotalDue = view.findViewById(R.id.tvTotalDue);
        tvNextDue = view.findViewById(R.id.tvNextDue);
        fabAddBilling = view.findViewById(R.id.fabAddBilling);

        recyclerViewBillings.setLayoutManager(new LinearLayoutManager(getContext()));

        if (sessionManager.isAdmin()) {
            fabAddBilling.setVisibility(View.VISIBLE);
            fabAddBilling.setOnClickListener(v -> showAddBillingDialog(null));
        } else {
            fabAddBilling.setVisibility(View.GONE);
        }

        loadBillings();

        return view;
    }

    private void loadBillings() {
        db.collection("billings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    billingList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Billing billing = doc.toObject(Billing.class);
                        if (billing != null) {
                            billing.setId(doc.getId());
                            billingList.add(billing);
                        }
                    }

                    if (billingList.isEmpty()) {
                        tvEmptyBillings.setVisibility(View.VISIBLE);
                        recyclerViewBillings.setVisibility(View.GONE);
                        tvTotalDue.setText("₱0");
                        tvNextDue.setText("₱0");
                    } else {
                        tvEmptyBillings.setVisibility(View.GONE);
                        recyclerViewBillings.setVisibility(View.VISIBLE);

                        billingAdapter = new BillingAdapter(billingList, sessionManager.isAdmin(), this);
                        recyclerViewBillings.setAdapter(billingAdapter);

                        calculateTotals();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading billings: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateTotals() {
        double totalDue = 0;
        double nextDue = 0;
        boolean foundNext = false;

        for (Billing billing : billingList) {
            if (!billing.getStatus().equalsIgnoreCase("Paid")) {
                totalDue += billing.getAmount();
                if (!foundNext) {
                    nextDue = billing.getAmount();
                    foundNext = true;
                }
            }
        }

        tvTotalDue.setText("₱" + String.format("%.2f", totalDue));
        tvNextDue.setText("₱" + String.format("%.2f", nextDue));
    }

    private void showAddBillingDialog(Billing billing) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_billing, null);

        TextView tvDialogBillingTitle = dialogView.findViewById(R.id.tvDialogBillingTitle);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etDialogBillingCategory);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etDialogBillingAmount);

        // FIX APPLIED HERE: Changed TextInputEditText to AutoCompleteTextView
        AutoCompleteTextView etStatus = dialogView.findViewById(R.id.etDialogBillingStatus);

        TextInputEditText etDueDate = dialogView.findViewById(R.id.etDialogBillingDueDate);
        Button btnCancel = dialogView.findViewById(R.id.btnBillingCancel);
        Button btnSave = dialogView.findViewById(R.id.btnBillingSave);

        // Setup status dropdown
        String[] statuses = {"Pending", "Paid", "Overdue"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, statuses);

        // This line now works:
        etStatus.setAdapter(statusAdapter);

        etStatus.setKeyListener(null); // make non-editable, acts like dropdown

        // Setup due date picker
        etDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> etDueDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        if (billing != null) {
            tvDialogBillingTitle.setText("Edit Billing");
            etCategory.setText(billing.getCategory());
            etAmount.setText(String.valueOf(billing.getAmount()));
            etStatus.setText(billing.getStatus());
            etDueDate.setText(billing.getDueDate());
        }

        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String category = etCategory.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String status = etStatus.getText().toString().trim();
            String dueDate = etDueDate.getText().toString().trim();

            if (category.isEmpty()) { etCategory.setError("Category is required"); return; }
            if (amountStr.isEmpty()) { etAmount.setError("Amount is required"); return; }
            if (status.isEmpty()) { etStatus.setError("Status is required"); return; }
            if (dueDate.isEmpty()) { etDueDate.setError("Due date is required"); return; }

            double amount;
            try { amount = Double.parseDouble(amountStr); } catch (Exception e) { etAmount.setError("Invalid amount"); return; }

            if (billing == null) {
                Billing newBilling = new Billing(category, amount, status, dueDate);
                db.collection("billings")
                        .add(newBilling)
                        .addOnSuccessListener(docRef -> {
                            newBilling.setId(docRef.getId());
                            Toast.makeText(getContext(), "Billing added successfully!", Toast.LENGTH_SHORT).show();
                            loadBillings();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                billing.setCategory(category);
                billing.setAmount(amount);
                billing.setStatus(status);
                billing.setDueDate(dueDate);

                db.collection("billings").document(billing.getId())
                        .set(billing)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Billing updated successfully!", Toast.LENGTH_SHORT).show();
                            loadBillings();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onEditClick(Billing billing) {
        showAddBillingDialog(billing);
    }

    @Override
    public void onDeleteClick(Billing billing) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Billing")
                .setMessage("Are you sure you want to delete this billing record?")
                .setPositiveButton("Delete", (dialog, which) ->
                        db.collection("billings").document(billing.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> loadBillings())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                )
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}