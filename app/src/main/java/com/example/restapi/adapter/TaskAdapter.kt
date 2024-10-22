package com.example.restapi.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restapi.R
import com.example.restapi.model.Task

class TaskAdapter(
    private var tasks: MutableList<Task>,
    private val deleteTask: (Task) -> Unit,
    private val startUpdateTaskActivity: (Task) -> Unit,
    private val imageUpload: (Int) -> Unit,
    private val updateTaskStatus: (Task) -> Unit, // Function to handle task status update
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val taskId: TextView = itemView.findViewById(R.id.taskId)
        val taskStatus: CheckBox =
            itemView.findViewById(R.id.taskStatus) // Checkbox for task status
        val imgPreview: ImageView = itemView.findViewById(R.id.taskImg)
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskTime: TextView = itemView.findViewById(R.id.taskTime)
        val actionButton: Button = itemView.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.apply {
            taskId.text = "${task.id}. "
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskTime.text = task.taskTime
            taskStatus.apply {
                setOnCheckedChangeListener(null)
                isChecked = task.completed == "1"
                setOnCheckedChangeListener { _, isChecked ->
                    // Prevent further interaction
                    if (alpha == 0.4f) return@setOnCheckedChangeListener
                    task.completed = if (isChecked) "1" else "0"
                    updateTaskStatus(task)
                    alpha = 0.4f
                    isClickable = false // Prevent further clicks
                    Handler(Looper.getMainLooper()).postDelayed({
                        alpha = 1.0f
                        isClickable = true // Re-enable clicks
                    }, 2000)
                }
            }
            Glide.with(holder.itemView.context)
                .load(task.img)
                .placeholder(R.drawable.loading) // Placeholder image
                .error(R.drawable.loading) // Error image
                .centerCrop()
                .into(holder.imgPreview)

            actionButton.setOnClickListener { showActionDialog(itemView.context, task) }
        }
    }

    private fun showActionDialog(context: Context, task: Task) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnUploadImage = dialogView.findViewById<Button>(R.id.imageUploadButton)
        val btnUpdate = dialogView.findViewById<Button>(R.id.updateButton)
        val btnDelete = dialogView.findViewById<Button>(R.id.deleteButton)

        btnUploadImage.setOnClickListener {
            if (task.id != null) {
                val taskId: Int = task.id!!.toInt() // Convert task.id to Int (assuming it's valid)
                imageUpload(taskId)
            }
            dialog.dismiss()
        }

        btnUpdate.setOnClickListener {
            startUpdateTaskActivity(task)
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Are you Sure?")
                .setMessage("Do you want to delete this task?")
                .setIcon(R.drawable.baseline_delete_forever_24)
                .setPositiveButton("Delete") { _, _ ->
                    Thread {
                        deleteTask(task) // Delete task in a background thread to avoid blocking the UI
                    }.start()
                }
                .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun getItemCount(): Int = tasks.size

    fun setTasks(tasks: List<Task>) {
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

    fun updateTaskState(updatedTask: Task) {
        val position =
            tasks.indexOfFirst { it.id == updatedTask.id } // Find the index of the task by ID
        if (position >= 0) {
            tasks[position] = updatedTask // Update the task in the list
            notifyItemChanged(position) // Notify that the item has changed
        }
    }
}