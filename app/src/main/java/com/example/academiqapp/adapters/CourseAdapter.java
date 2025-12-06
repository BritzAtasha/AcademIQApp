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
import com.example.academiqapp.models.Course;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private boolean isAdmin;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEditClick(Course course);
        void onDeleteClick(Course course);
    }

    public CourseAdapter(List<Course> courseList, boolean isAdmin, OnCourseActionListener listener) {
        this.courseList = courseList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvCourseName.setText(course.getCourseName());
        holder.tvCourseCode.setText(course.getCourseCode());
        holder.tvInstructor.setText("Instructor: " + course.getInstructor());
        holder.tvDescription.setText(course.getDescription());

        if (isAdmin) {
            holder.llAdminActions.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(course);
            });
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(course);
            });
        } else {
            holder.llAdminActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void updateCourses(List<Course> newCourses) {
        this.courseList = newCourses;
        notifyDataSetChanged();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCourseCode, tvInstructor, tvDescription;
        LinearLayout llAdminActions;
        Button btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            llAdminActions = itemView.findViewById(R.id.llAdminActions);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
