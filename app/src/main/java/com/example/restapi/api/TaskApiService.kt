package com.example.restapi.api

import com.example.restapi.model.Task
import retrofit2.Call
import retrofit2.http.*

interface TaskApiService {

    // Retrieve all tasks
    @GET("tasks")
    fun getTasks(): Call<List<Task>>

    // Retrieve a specific task by ID
    @GET("tasks/{id}")
    fun getTask(@Path("id") id: Int): Call<Task>

    // Create a new task
    @POST("tasks/hello-tasks/store")
    fun createTask(@Body task: Task): Call<Task>

    // Update a specific task (PUT)
    @PUT("tasks/{id}")
    fun updateTask(@Path("id") id: Int, @Body task: Task): Call<Task>

    // Update a specific task (PATCH)
    @PATCH("tasks/{id}")
    fun patchTask(@Path("id") id: Int, @Body task: Task): Call<Task>

    // Delete a specific task
    @DELETE("tasks/{id}")
    fun deleteTask(@Path("id") id: Int): Call<Unit>
}
