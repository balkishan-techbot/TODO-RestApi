package com.example.restapi

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restapi.adapter.CompletedTaskAdapter
import com.example.restapi.adapter.TaskAdapter
import com.example.restapi.api.TaskClient
import com.example.restapi.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_UPDATE = 1
    }

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: CompletedTaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var completedTaskView: RecyclerView
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var taskTimeInput: TextView
    private lateinit var addButton: Button
    private lateinit var selectTimeButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerViews
        recyclerView = findViewById(R.id.recyclerView)
        completedTaskView = findViewById(R.id.completedTaskView)

        //initialize variables
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        addButton = findViewById(R.id.addButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        taskTimeInput = findViewById(R.id.taskTimeInput)


        // Set up RecyclerViews
        taskAdapter = TaskAdapter(
            mutableListOf(),
            ::deleteTask,
            ::startUpdateTaskActivity,
            ::imageUpload,
            ::updateTaskStatus
        )
        completedTaskAdapter = CompletedTaskAdapter(mutableListOf(), ::deleteTask, ::updateTaskStatus)

        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        completedTaskView.adapter = completedTaskAdapter
        completedTaskView.layoutManager = LinearLayoutManager(this)

        // Load tasks
        loadTasks()

        //click Listeners
        selectTimeButton.setOnClickListener {
            showDateTimePicker()
        }
        addButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val taskTime = taskTimeInput.text.toString()
            if (title.isNotEmpty() && description.isNotEmpty() && taskTime.isNotEmpty()) {
                val task = Task(id = null, title = title, description = description, taskTime = taskTime)
                createTask(task)
            } else {
                showToast("Please fill in all fields")
            }
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
    fun Context.showLoadingDialog(): ProgressDialog {
        return ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false) // Prevent dismissal on back press
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            show()
        }
    }
    private fun loadTasks() {
        // Show loading dialog
        val dialog = showLoadingDialog()
        Thread {
            try {
                val response = TaskClient.taskApi.getTasks().execute()
                runOnUiThread {
                        dialog.dismiss()
                    if (response.isSuccessful) {
                        response.body()?.let { tasks ->
                            val incompleteTasks = tasks.filter { it.completed == "0" }
                            val completedTasks = tasks.filter { it.completed == "1" }
                            taskAdapter.setTasks(incompleteTasks)
                            completedTaskAdapter.setCompletedTasks(completedTasks)
                        } ?: showToast("No tasks found")
                    } else {

                        showToast("Failed to load tasks")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {  dialog.dismiss()
                    showToast("Error loading tasks")
                }
            }
        }.start()
    }


    private fun createTask(task: Task) {
        val dialog = showLoadingDialog()
        Thread {
            val response = TaskClient.taskApi.createTask(task).execute()
            runOnUiThread {  dialog.dismiss()
                if (response.isSuccessful) {
                    response.body()?.let { newTask ->
                        taskAdapter.addTask(newTask)
                        showToast("Task created")
                        clearInputFields()
                    }
                } else {
                    showToast("Failed to create task")
                }
            }
        }.start()
    }

    private fun deleteTask(task: Task) {
        Thread {
            val response = TaskClient.taskApi.deleteTask(task.id ?: -1).execute()
            runOnUiThread {
                if (response.isSuccessful) {
                    taskAdapter.removeTask(task)
                    completedTaskAdapter.removeTask(task)
                    showToast("Task deleted")
                } else {
                    showToast("Failed to delete task")
                }
            }
        }.start()
    }

    private fun updateTaskStatus(task: Task) {
        // Synchronize to prevent multiple clicks causing concurrent modifications
        synchronized(this) {
            Thread {
                val response = TaskClient.taskApi.updateTask(task.id ?: -1, task).execute()
                runOnUiThread {
                    if (response.isSuccessful) {
                        val updatedTask = response.body()
                        if (updatedTask != null) {
                            loadTasks()
                            showToast("Task status updated successfully")
                        } else {
                            showToast("Failed to update task status")
                        }
                    } else {
                        showToast("Something went wrong. Please try again.")
                    }
                }
            }.start()
        }
    }


    private fun imageUpload(taskId: Int) {
        val intent = Intent(this, ImageUploadActivity::class.java)
        intent.putExtra("TASK_ID", taskId)
        startActivityForResult(intent, REQUEST_CODE_UPDATE)
    }

    private fun startUpdateTaskActivity(task: Task) {
        val intent = Intent(this, UpdateTaskActivity::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DESCRIPTION", task.description)
            putExtra("TASK_TIME", task.taskTime)
        }
        startActivityForResult(intent, REQUEST_CODE_UPDATE) // Handle results in onActivityResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK) {
            showToast("Task updated SuccessFully")
            loadTasks()
            taskAdapter.notifyDataSetChanged()
            // Update your local list and refresh the UI
        }
    }

    private fun clearInputFields() {
        titleInput.text.clear()
        descriptionInput.text.clear()
        //hides Keyboard
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    private fun loadTasks() {
//        TaskClient.taskApi.getTasks().enqueue(object : Callback<List<Task>> {
//            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { tasks ->
//                        val incompleteTasks = tasks.filter { it.completed == "0" }
//                        val completedTasks = tasks.filter { it.completed == "1"}
//
//                        taskAdapter.setTasks(incompleteTasks) // Set incomplete tasks
//                        completedTaskAdapter.setCompletedTasks(completedTasks) // Set completed tasks
//                    } ?: showToast("No tasks found")
//                } else {
//                    showToast("Failed to load tasks")
//                }
//            }
//            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//    private fun createTask(task: Task) {
//            TaskClient.taskApi.createTask(task).enqueue(object : Callback<Task> {
//                override fun onResponse(call: Call<Task>, response: Response<Task>) {
//                    if (response.isSuccessful) {
//                        response.body()?.let { newTask ->
//                            taskAdapter.addTask(newTask)
//                            showToast("Task created")
//                            clearInputFields()
//                        }
//                    } else {
//                        Log.e("Task Creation Error", response.errorBody()?.string() ?: "Unknown error")
//                        showToast("Failed to create task")
//                    }
//                }
//
//                override fun onFailure(call: Call<Task>, t: Throwable) {
//                    showToast("Error: ${t.message}")
//                }
//            })
//        }
//    private fun deleteTask(task: Task) {
//        // Make the network call to delete the task from the server
//        TaskClient.taskApi.deleteTask(task.id ?: -1).enqueue(object : Callback<Unit> {
//            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
//                if (response.isSuccessful) {
//                    taskAdapter.removeTask(task) // Remove task from RecyclerView
//                    completedTaskAdapter.removeTask(task)// Remove task from RecyclerView
//                    showToast("Task deleted")
//                } else {
//                    showToast("Failed to delete task")
//                }
//            }
//            override fun onFailure(call: Call<Unit>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//    private fun updateTaskStatus(task: Task) {
//        // Use PUT to update the entire task
//        TaskClient.taskApi.updateTask(task.id ?: -1, task).enqueue(object : Callback<Task> {
//            override fun onResponse(call: Call<Task>, response: Response<Task>) {
//                if (response.isSuccessful) {
//                    val updatedTask = response.body()
//                    if (updatedTask != null) {
//                        // Update the UI for completed and incomplete tasks
//                        if (task.completed == "1") {
//                            taskAdapter.removeTask(task)
//                            completedTaskAdapter.addTask(task)
//                        }else{
//                            completedTaskAdapter.removeTask(task)
//                        }
//                        showToast("Task status updated successfully")
//                    } else { showToast("Failed to update task status") }
//                } else {
//                    Log.e("Task Update Error", response.errorBody()?.string() ?: "Unknown error")
//                    showToast("Something went wrong. Please try again.")
//                }
//            }
//            override fun onFailure(call: Call<Task>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
}