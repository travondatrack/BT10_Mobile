package com.example.bai10

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bai10.R

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: TodoAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        db = DatabaseHelper(this)

        val edtTask = findViewById<EditText>(R.id.edtNewTask)
        val btnAdd = findViewById<Button>(R.id.btnAddTask)
        val recyclerView = findViewById<RecyclerView>(R.id.rvTasks)

        // Setup RecyclerView
        adapter = TodoAdapter(arrayListOf(),
            onDeleteClick = { taskId ->
                db.deleteTask(taskId)
                loadTasks()
            },
            onStatusChange = { taskId, isDone ->
                db.updateTaskStatus(taskId, isDone)
            },
            onEditClick = { taskId, currentContent ->
                // show dialog to edit task
                val editText = android.widget.EditText(this).apply { setText(currentContent) }
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Sửa công việc")
                    .setView(editText)
                    .setPositiveButton("Lưu") { dialog, _ ->
                        val newContent = editText.text.toString()
                        if (newContent.isNotBlank()) {
                            db.updateTaskContent(taskId, newContent)
                            loadTasks()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load dữ liệu ban đầu
        loadTasks()

        btnAdd.setOnClickListener {
            val content = edtTask.text.toString()
            if (content.isNotEmpty()) {
                val newTask = Task(userId = currentUserId, content = content)
                db.addTask(newTask)
                edtTask.text.clear()
                loadTasks()
            }
        }
    }

    private fun loadTasks() {
        val list = db.getAllTasks(currentUserId)
        adapter.updateList(list)
    }
}