package com.example.bai10

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bai10.R
import com.example.bai10.Task

// Adapter để binding dữ liệu vào RecyclerView
class TodoAdapter(
    private val tasks: ArrayList<Task>,
    private val onDeleteClick: (Int) -> Unit,
    private val onStatusChange: (Int, Boolean) -> Unit,
    private val onEditClick: (Int, String) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val cbStatus: CheckBox = view.findViewById(R.id.cbStatus)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvContent.text = task.content
        holder.cbStatus.isChecked = task.isCompleted

        // Gạch ngang nếu đã hoàn thành
        toggleStrikeThrough(holder.tvContent, task.isCompleted)

        holder.cbStatus.setOnCheckedChangeListener { _, isChecked ->
            toggleStrikeThrough(holder.tvContent, isChecked)
            onStatusChange(task.id, isChecked)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(task.id, task.content)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(task.id)
        }
    }

    private fun toggleStrikeThrough(tv: TextView, isChecked: Boolean) {
        if (isChecked) {
            tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount() = tasks.size

    fun updateList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}