package com.example.restapi.api

import com.example.restapi.model.Task
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {

    // Retrieve all tasks
    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>

    // Retrieve a specific task by ID
    @GET("tasks/{id}")
    fun getTask(@Path("id") id: Int): Call<Task>

    // Create a new task
    @POST("tasks/hello-tasks/store")
    suspend fun createTask(@Body task: Task): Response<Task>

    // Update a specific task (PUT)
    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Response<Task>

    // Update a specific task (PATCH)
    @PATCH("tasks/{id}")
    suspend fun patchTask(@Path("id") id: Int, @Body task: Task):Response<Task>

    // Delete a specific task
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
