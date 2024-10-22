package com.example.restapi.model

import com.google.gson.annotations.SerializedName

data class Task(
    var id: Int? = null, // Or any other type
    var title: String,
    var description: String,
    var completed: String = "0",
    @SerializedName("task_time") var taskTime: String,  // Add taskTime as a String or another appropriate type
) {
    val img: String
        get() = "https://beautycare.techbot.pro/api/image/$id" // Construct the image URL
}
