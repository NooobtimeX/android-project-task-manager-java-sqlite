package com.example.taskmanager;

public class Task {
    private int id;             // Unique identifier for each task
    private String title;       // Task title
    private boolean completed;  // Completion status of the task
    private String dueDate;     // Due date for the task in String format (e.g., "2023-12-31")

    // Constructor for all fields
    public Task(int id, String title, boolean completed, String dueDate) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    // Constructor for new tasks (without ID)
    public Task(String title, boolean completed, String dueDate) {
        this.title = title;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}

