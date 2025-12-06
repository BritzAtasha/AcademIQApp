package com.example.academiqapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academiqapp.R;
import com.example.academiqapp.models.Billing;

import java.util.List;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.BillingViewHolder> {

    private final List<Billing> billingList;
    private final boolean isAdmin;
    private final OnBillingActionListener listener;

    public interface OnBillingActionListener {
        void onEditClick(Billing billing);
        void onDeleteClick(Billing billing);
    }

    public BillingAdapter(List<Billing> billingList, boolean isAdmin, OnBillingActionListener listener) {
        this.billingList = billingList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing, parent, false);
        return new BillingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {
        Billing billing = billingList.get(position);

        holder.tvCategory.setText(billing.getCategory());
        holder.tvAmount.setText("₱" + String.format("%.2f", billing.getAmount()));
        holder.tvDueDate.setText("Due: " + billing.getDueDate());
        holder.tvStatus.setText("Status: " + billing.getStatus());

        if (isAdmin) {
            holder.llActions.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(billing);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(billing);
            });
        } else {
            holder.llActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return billingList.size();
    }

    public void updateBillings(List<Billing> newBillings) {
        billingList.clear();
        billingList.addAll(newBillings);
        notifyDataSetChanged();
    }

    static class BillingViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDueDate, tvStatus;
        LinearLayout llActions;
        Button btnEdit, btnDelete;

        public BillingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBillingCategory);
            tvAmount = itemView.findViewById(R.id.tvBillingAmount);
            tvDueDate = itemView.findViewById(R.id.tvBillingDueDate);
            tvStatus = itemView.findViewById(R.id.tvBillingStatus);
            llActions = itemView.findViewById(R.id.llBillingActions);
            btnEdit = itemView.findViewById(R.id.btnEditBilling);
            btnDelete = itemView.findViewById(R.id.btnDeleteBilling);
        }
    }
}
