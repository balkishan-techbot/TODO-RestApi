package com.example.restapi.model

import com.google.gson.annotations.SerializedName

data class Image(
    var id: Int? = null,            // Change to Int based on your JSON
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_path") val filePath: String,
    @SerializedName("task_id") val taskId: Int,
)