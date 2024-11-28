package com.example.taskmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;

    private Spinner filterSpinner;
    private ListView taskListView;
    private FloatingActionButton createTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filterSpinner = findViewById(R.id.filterSpinner);
        taskListView = findViewById(R.id.taskListView);
        createTaskButton = findViewById(R.id.createTaskButton);

        dbHelper = new TaskDatabaseHelper(this);
        taskList = dbHelper.getAllTasks();
        taskAdapter = new TaskAdapter(this, taskList);

        taskListView.setAdapter(taskAdapter);

        // Setup filter spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"All Tasks", "Completed", "Incomplete"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTasks(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Floating button to create a task
        createTaskButton.setOnClickListener(v -> showCreateTaskDialog());

        // Toggle task completion on click
        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);
            task.setCompleted(!task.isCompleted());
            dbHelper.updateTask(task);
            refreshTaskList(); // Refresh after toggling completion
        });

        // Long click to delete task
        taskListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);
            dbHelper.deleteTask(task.getId());
            refreshTaskList(); // Refresh after deletion
            return true;
        });
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

                Task task = new Task(taskTitle, false, dueDate);
                dbHelper.addTask(task);
                refreshTaskList();
                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void filterTasks(int filterType) {
        taskList.clear();

        switch (filterType) {
            case 0: // All Tasks
                taskList.addAll(dbHelper.getAllTasks());
                break;
            case 1: // Completed Tasks
                taskList.addAll(dbHelper.getFilteredTasks(true));
                break;
            case 2: // Incomplete Tasks
                taskList.addAll(dbHelper.getFilteredTasks(false));
                break;
        }

        taskAdapter.notifyDataSetChanged();
    }

    public void refreshTaskList() {
        taskList.clear();
        taskList.addAll(dbHelper.getAllTasks());
        taskAdapter.notifyDataSetChanged();
    }
}
