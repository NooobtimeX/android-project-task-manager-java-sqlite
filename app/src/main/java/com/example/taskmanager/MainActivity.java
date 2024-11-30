package com.example.taskmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private ListView taskListView;
    private FloatingActionButton createTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        taskListView = findViewById(R.id.taskListView);
        createTaskButton = findViewById(R.id.createTaskButton);

        // Initialize database helper and load grouped tasks
        dbHelper = new TaskDatabaseHelper(this);
        taskList = dbHelper.getTasksGroupedByCompletion();

        // Set up the adapter
        taskAdapter = new TaskAdapter(this, taskList);
        taskListView.setAdapter(taskAdapter);

        // Floating button to create a task
        createTaskButton.setOnClickListener(v -> showCreateTaskDialog());

        // Toggle task completion on click
        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);

            // Skip if the item is a header
            if (task.getId() == -1) return;

            // Toggle completion and update the database
            task.setCompleted(!task.isCompleted());
            dbHelper.updateTask(task);
            refreshTaskList();
        });

        // Long click to delete task
        taskListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);

            // Skip if the item is a header
            if (task.getId() == -1) return true;

            // Delete the task and refresh the list
            dbHelper.deleteTask(task.getId());
            refreshTaskList();
            return true;
        });
    }

    public void updateTask(Task task) {
        dbHelper.updateTask(task);
        refreshTaskList(); // Optionally refresh the list after updating
    }

    private void showCreateTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_task);

        EditText editTaskTitle = dialog.findViewById(R.id.editDialogTaskTitle);
        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Button createButton = dialog.findViewById(R.id.dialogCreateButton);
        Button closeButton = dialog.findViewById(R.id.dialogCloseButton);

        createButton.setOnClickListener(v -> {
            String taskTitle = editTaskTitle.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1; // Months are 0-based
                int year = datePicker.getYear();
                String dueDate = year + "-" + month + "-" + day;

                // Add new task and refresh
                Task task = new Task(taskTitle, false, dueDate);
                dbHelper.addTask(task);
                refreshTaskList();
                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void refreshTaskList() {
        taskList.clear();
        taskList.addAll(dbHelper.getTasksGroupedByCompletion());
        taskAdapter.notifyDataSetChanged();
    }
}
