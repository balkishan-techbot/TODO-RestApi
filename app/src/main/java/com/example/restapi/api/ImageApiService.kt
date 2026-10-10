package com.example.restapi.api

import com.example.restapi.model.Image
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ImageApiService {

    // Upload an image
    @Multipart
    @POST("upload-image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("task_id") taskId: RequestBody
    ): Response<ResponseBody>

    // Fetch an image by ID
    @GET("image/{id}")
    fun getImageById(
        @Path("id") id: String,
    ): Call<ResponseBody>

    @GET("images")
    fun getAllImages(): Call<List<Image>>
}
