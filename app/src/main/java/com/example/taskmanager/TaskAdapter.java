package com.example.taskmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Task> taskList;
    private boolean isIncompleteVisible = true; // Default visibility
    private boolean isCompletedVisible = true; // Default visibility

    public TaskAdapter(Context context, ArrayList<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        Task task = taskList.get(position);
        return task.getId() == -1 ? 0 : 1; // 0 = Header, 1 = Task
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task task = taskList.get(position);

        if (getItemViewType(position) == 0) {
            // Header View
            convertView = LayoutInflater.from(context).inflate(R.layout.task_header, parent, false);
            TextView headerTitle = convertView.findViewById(R.id.headerTitle);
            Button toggleButton = convertView.findViewById(R.id.toggleButton);

            headerTitle.setText(task.getTitle());
            if (task.getTitle().equals("Incomplete")) {
                toggleButton.setText(isIncompleteVisible ? "Hide" : "Show");
                toggleButton.setOnClickListener(v -> {
                    isIncompleteVisible = !isIncompleteVisible;
                    notifyDataSetChanged(); // Refresh the list
                });
            } else if (task.getTitle().equals("Completed")) {
                toggleButton.setText(isCompletedVisible ? "Hide" : "Show");
                toggleButton.setOnClickListener(v -> {
                    isCompletedVisible = !isCompletedVisible;
                    notifyDataSetChanged(); // Refresh the list
                });
            }
        } else {
            // Task Item View
            if ((task.isCompleted() && !isCompletedVisible) || (!task.isCompleted() && !isIncompleteVisible)) {
                convertView = new View(context); // Empty view when hidden
                convertView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);

                CheckBox taskCompleted = convertView.findViewById(R.id.taskCompleted);
                TextView taskTitle = convertView.findViewById(R.id.taskTitle);
                TextView taskDueDate = convertView.findViewById(R.id.taskDueDate);

                taskTitle.setText(task.getTitle());
                taskDueDate.setText(task.getDueDate());
                taskCompleted.setChecked(task.isCompleted());

                taskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    task.setCompleted(isChecked);
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateTask(task);
                    }
                });
            }
        }

        return convertView;
    }
}
