package com.example.taskmanager;

import android.app.Dialog;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private TaskDatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;

    private Spinner sortSpinner;
    private ListView taskListView;
    private FloatingActionButton createTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        sortSpinner = findViewById(R.id.sortSpinner);
        taskListView = findViewById(R.id.taskListView);
        createTaskButton = findViewById(R.id.createTaskButton);

        // Initialize database helper and task list
        dbHelper = new TaskDatabaseHelper(this);
        taskList = dbHelper.getTasksGroupedByCompletion();

        // Set up the adapter
        taskAdapter = new TaskAdapter(this, taskList);
        taskListView.setAdapter(taskAdapter);

        // Floating button to create a task
        createTaskButton.setOnClickListener(v -> showCreateTaskDialog());

        // Sort options for the Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Sort by Due Date (Ascending)", "Sort by Due Date (Descending)"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        // Handle sort selection
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) {
                    sortTasksByDueDate(true); // Ascending
                } else {
                    sortTasksByDueDate(false); // Descending
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

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

    private void refreshTaskList() {
        taskList.clear();
        taskList.addAll(dbHelper.getTasksGroupedByCompletion());
        taskAdapter.notifyDataSetChanged();
    }

    private void sortTasksByDueDate(boolean ascending) {
        refreshTaskList(); // Get a fresh list with headers

        // Separate tasks into completed and incomplete groups
        ArrayList<Task> incompleteTasks = new ArrayList<>();
        ArrayList<Task> completedTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getId() == -1) continue; // Skip headers
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                incompleteTasks.add(task);
            }
        }

        // Sort each group by due date
        Comparator<Task> dateComparator = (t1, t2) -> {
            if (ascending) {
                return t1.getDueDate().compareTo(t2.getDueDate());
            } else {
                return t2.getDueDate().compareTo(t1.getDueDate());
            }
        };
        Collections.sort(incompleteTasks, dateComparator);
        Collections.sort(completedTasks, dateComparator);

        // Rebuild the task list with headers
        taskList.clear();
        taskList.add(new Task(-1, "Incomplete", false, ""));
        taskList.addAll(incompleteTasks);
        taskList.add(new Task(-1, "Completed", true, ""));
        taskList.addAll(completedTasks);

        taskAdapter.notifyDataSetChanged();
    }

    public void updateTask(Task task) {
        dbHelper.updateTask(task); // Update the task in the database
        refreshTaskList();         // Refresh the list after updating
    }
    public void showEditTaskDialog(Task task) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_task); // Reuse the same layout

        EditText editTaskTitle = dialog.findViewById(R.id.editDialogTaskTitle);
        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Button saveButton = dialog.findViewById(R.id.dialogCreateButton);
        Button closeButton = dialog.findViewById(R.id.dialogCloseButton);

        // Pre-fill existing task details
        editTaskTitle.setText(task.getTitle());
        String[] dateParts = task.getDueDate().split("-");
        datePicker.updateDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));

        saveButton.setOnClickListener(v -> {
            String updatedTitle = editTaskTitle.getText().toString().trim();
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            String updatedDueDate = year + "-" + month + "-" + day;

            if (!updatedTitle.isEmpty()) {
                task.setTitle(updatedTitle);
                task.setDueDate(updatedDueDate);
                updateTask(task); // Update in the database and refresh list
                dialog.dismiss();
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    public void deleteTask(Task task) {
        dbHelper.deleteTask(task.getId());
        refreshTaskList();
    }
}
