package com.example.taskmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TaskDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, completed INTEGER, due_date TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", task.getTitle());
        values.put("completed", task.isCompleted() ? 1 : 0);
        values.put("due_date", task.getDueDate());
        db.insert("tasks", null, values);
        db.close();
    }

    public ArrayList<Task> getTasksGroupedByCompletion() {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Incomplete tasks
        tasks.add(new Task(-1, "Incomplete", false, ""));
        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE completed = 0", null);
        while (cursor.moveToNext()) {
            tasks.add(new Task(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) == 1, cursor.getString(3)));
        }
        cursor.close();

        // Completed tasks
        tasks.add(new Task(-1, "Completed", true, ""));
        cursor = db.rawQuery("SELECT * FROM tasks WHERE completed = 1", null);
        while (cursor.moveToNext()) {
            tasks.add(new Task(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) == 1, cursor.getString(3)));
        }
        cursor.close();

        db.close();
        return tasks;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", task.getTitle());
        values.put("completed", task.isCompleted() ? 1 : 0);
        values.put("due_date", task.getDueDate());
        db.update("tasks", values, "id = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
