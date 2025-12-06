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
import com.example.academiqapp.models.Grade;

import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<Grade> gradeList;
    private boolean isAdmin;
    private OnGradeActionListener listener;

    public interface OnGradeActionListener {
        void onEditClick(Grade grade);
        void onDeleteClick(Grade grade);
    }

    public GradeAdapter(List<Grade> gradeList, boolean isAdmin, OnGradeActionListener listener) {
        this.gradeList = gradeList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = gradeList.get(position);

        holder.tvGradeCourseCode.setText(grade.getCourseCode());
        holder.tvGradeCourseName.setText(grade.getCourseName());
        holder.tvGradeUnits.setText("Units: " + grade.getUnits());
        holder.tvGradeValue.setText(String.valueOf(grade.getGradeValue()));

        if (isAdmin) {
            holder.llAdminActions.setVisibility(View.VISIBLE);

            holder.btnEditGrade.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(grade);
            });

            holder.btnDeleteGrade.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(grade);
            });
        } else {
            holder.llAdminActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return gradeList.size(); }

    public void updateGrades(List<Grade> newGrades) {
        this.gradeList = newGrades;
        notifyDataSetChanged();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvGradeCourseCode, tvGradeCourseName, tvGradeUnits, tvGradeValue;
        LinearLayout llAdminActions;
        Button btnEditGrade, btnDeleteGrade;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGradeCourseCode = itemView.findViewById(R.id.tvGradeCourseCode);
            tvGradeCourseName = itemView.findViewById(R.id.tvGradeCourseName);
            tvGradeUnits = itemView.findViewById(R.id.tvGradeUnits);
            tvGradeValue = itemView.findViewById(R.id.tvGradeValue);
            llAdminActions = itemView.findViewById(R.id.llAdminActions);
            btnEditGrade = itemView.findViewById(R.id.btnEditGrade);
            btnDeleteGrade = itemView.findViewById(R.id.btnDeleteGrade);
        }
    }
}
