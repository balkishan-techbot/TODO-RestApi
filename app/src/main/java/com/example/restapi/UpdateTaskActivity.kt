package com.example.restapi

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.restapi.api.TaskClient
import com.example.restapi.model.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateTaskActivity : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var taskTimeInput: TextView
    private lateinit var selectTimeButton: Button
    private var taskId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        titleInput = findViewById(R.id.updateTitleInput)
        descriptionInput = findViewById(R.id.updateDescriptionInput)
        taskTimeInput = findViewById(R.id.taskTimeInput)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        taskTimeInput = findViewById(R.id.taskTimeInput)
        // Retrieve task details from the intent
        taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE")
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION")
        val taskTime = intent.getStringExtra("TASK_TIME")
        // Populate the input fields with task data
        titleInput.setText(taskTitle)
        descriptionInput.setText(taskDescription)
        taskTimeInput.text = taskTime

        selectTimeButton.setOnClickListener {
            showDateTimePicker()
        }
        // Update button to save changes
        val updateButton: Button = findViewById(R.id.updateButton)
        updateButton.setOnClickListener {
            updateTask() // Call the update task function
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)

                val formattedDateTime = dateFormat.format(calendar.time) // Correct format
                taskTimeInput.text = formattedDateTime // Set the correctly formatted date-time
                Log.e("task input", taskTimeInput.toString())
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, year, month, day).show()
    }

    private fun Context.showLoadingDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false) // Prevent dismissal on back press
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            show()
        }
    }
    private fun updateTask() {
        // Ensure taskId is valid
        if (taskId != null && taskId!! >= 0) {
            val updatedTask = Task(
                id = taskId,
                title = titleInput.text.toString(),
                description = descriptionInput.text.toString(),
                taskTime = taskTimeInput.text.toString()
            )
            val dialog = showLoadingDialog()
            lifecycleScope.launch {
                try {
                    // Call the API to update the task
                    val response = TaskClient.taskApi.updateTask(updatedTask.id ?: -1, updatedTask)

                    // Dismiss the loading dialog
                    dialog.dismiss()

                    if (response.isSuccessful) {
                        // Pass updated data back to the previous activity
                        val resultIntent = Intent().apply {
                            putExtra("UPDATED_TITLE", updatedTask.title)
                            putExtra("UPDATED_DESCRIPTION", updatedTask.description)
                            putExtra("UPDATED_TIME", updatedTask.taskTime)
                        }
                        setResult(RESULT_OK, resultIntent) // Send updated data back
                        finish() // Close activity on success
                    } else {
                        showToast("Failed to update task")
                    }
                } catch (e: Exception) {
                    // Dismiss the loading dialog in case of an error
                    dialog.dismiss()
                    showToast("Error updating task: ${e.message}")
                }
            }
        } else {
            showToast("Invalid Task Selected")
        }
    }

    // Optional: Function to update using PATCH (not called in this activity)
    private fun patchTask() {
        if (taskId != null && taskId!! >= 0) {
            val taskUpdate = Task(
                id = taskId,
                title = titleInput.text.toString(),
                description = descriptionInput.text.toString(),
                taskTime = taskTimeInput.text.toString()
            )
            lifecycleScope.launch {
                try {
                    // Call the API to update the task
                    val response = TaskClient.taskApi.patchTask(taskId!!, taskUpdate)
                    if (response.isSuccessful) {
                        // Pass updated data back to the previous activity
                        val resultIntent = Intent().apply {
                            putExtra("UPDATED_TITLE", taskUpdate.title)
                            putExtra("UPDATED_DESCRIPTION", taskUpdate.description)
                            putExtra("UPDATED_TIME", taskUpdate.taskTime)
                        }
                        setResult(RESULT_OK, resultIntent) // Send updated data back
                        finish() // Close activity on success
                    } else {
                        showToast("Failed to update task")
                    }
                }catch (e: Exception) {
                    showToast("Error updating task: ${e.message}")
                }
        }} else {
            showToast("Invalid Task ID")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

