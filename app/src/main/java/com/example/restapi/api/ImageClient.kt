package com.example.restapi.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ImageClient {
    const val BASE_URL = "https://beautycare.techbot.pro/api/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val imageApi: ImageApiService = retrofit.create(ImageApiService::class.java)
}
