package com.example.restapi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.restapi.R
import com.example.restapi.model.Task

class CompletedTaskAdapter(
    private var tasks: MutableList<Task>,
    private val deleteTask: (Task) -> Unit,
    private val updateTaskStatus: (Task) -> Unit, // Use this parameter correctly
) : RecyclerView.Adapter<CompletedTaskAdapter.CompletedTaskViewHolder>() {

    inner class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskId: TextView = itemView.findViewById(R.id.taskId)
        val taskStatus: CheckBox =
            itemView.findViewById(R.id.taskStatus) // Checkbox for task status
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskTime: TextView = itemView.findViewById(R.id.taskTime)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.completed_task_list, parent, false)
        return CompletedTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskId.text = task.id.toString()
        holder.taskTitle.text = task.title
        holder.taskDescription.text = task.description
        holder.taskTime.text = task.taskTime

        holder.taskStatus.isChecked = (task.completed == "1")
        holder.taskStatus.setOnCheckedChangeListener(null)
        // Handle checkbox change
        holder.taskStatus.setOnCheckedChangeListener { _, isChecked ->
            task.completed = if (isChecked) "1" else "0"
            updateTaskStatus(task)
        }

        // Set delete button click listener
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Are you Sure?")
                .setMessage("Do you want to delete this task?")
                .setIcon(R.drawable.baseline_delete_forever_24)
                .setPositiveButton("Delete") { _, _ -> deleteTask(task) }
                .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun setCompletedTasks(tasks: List<Task>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
        notifyDataSetChanged()
    }

    fun addTask(task: Task) {
        this.tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun removeTask(task: Task) {
        val position = tasks.indexOf(task)
        if (position >= 0) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateTaskInList(updatedTask: Task) {
        val position =
            tasks.indexOfFirst { it.id == updatedTask.id } // Find the index of the task by ID
        if (position >= 0) {
            tasks[position] = updatedTask // Update the task in the list
            notifyItemChanged(position) // Notify that the item has changed
        }
    }
}