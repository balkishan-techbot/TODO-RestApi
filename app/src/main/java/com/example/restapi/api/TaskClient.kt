package com.example.restapi.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TaskClient {
    private const val BASE_URL = "https://beautycare.techbot.pro/api/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val taskApi: TaskApiService = retrofit.create(TaskApiService::class.java)
}
