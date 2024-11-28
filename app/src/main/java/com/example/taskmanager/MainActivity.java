package com.example.taskmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;

    private EditText editTaskTitle;
    private Button addTaskButton;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTaskTitle = findViewById(R.id.editTaskTitle);
        addTaskButton = findViewById(R.id.addTaskButton);
        filterSpinner = findViewById(R.id.filterSpinner);
        ListView taskListView = findViewById(R.id.taskListView);

        dbHelper = new TaskDatabaseHelper(this);
        taskList = dbHelper.getAllTasks();
        taskAdapter = new TaskAdapter(this, taskList);

        taskListView.setAdapter(taskAdapter);

        // Add a new task
        addTaskButton.setOnClickListener(v -> {
            String taskTitle = editTaskTitle.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                Task task = new Task(taskTitle, false);
                dbHelper.addTask(task);
                taskList.add(task);
                taskAdapter.notifyDataSetChanged();
                editTaskTitle.setText("");
            }
        });

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

        // Toggle completion on click
        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);
            task.setCompleted(!task.isCompleted());
            dbHelper.updateTask(task);
            taskAdapter.notifyDataSetChanged();
        });

        // Long click to delete task
        taskListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Task task = taskList.get(position);
            dbHelper.deleteTask(task.getId());
            taskList.remove(position);
            taskAdapter.notifyDataSetChanged();
            return true;
        });
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
}
