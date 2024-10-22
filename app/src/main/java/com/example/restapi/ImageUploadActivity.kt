package com.example.restapi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.restapi.api.ImageClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ImageUploadActivity : AppCompatActivity() {

    private lateinit var ivImagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnUploadImage: Button
    private var imageUri: Uri? = null // To store selected image URI
    private var taskId: Int? = null // To store task ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)

        // Initialize views
        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnUploadImage = findViewById(R.id.btnUploadImage)

        // Get the task ID from Intent or any source
        taskId = intent.getIntExtra(
            "TASK_ID",
            -1
        ) // Assuming task ID is passed from the previous activity

        // Set up click listeners
        btnSelectImage.setOnClickListener { selectImageFromGallery() }
        btnUploadImage.setOnClickListener {
            if (taskId == -1 || taskId == null) {
                showToast("Please select task to Upload image")
            } else {
                imageUri?.let { uploadImage(it, taskId!!) }
                    ?: showToast("Please select an image first")
            }
        }
    }

    // Function to open the gallery for image selection
    private fun selectImageFromGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Specify that we want to pick an image
        }
        resultLauncher.launch(pickImageIntent) // Launch the intent
    }

    // Handle result from image picker
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data // Get the URI of the selected image
                imageUri?.let {
                    Glide.with(this).load(it).into(ivImagePreview)
                } // Preview the selected image
            }
        }

    // Function to upload the image directly using Uri
    private fun uploadImage(uri: Uri, taskId: Int) {
        val filePath = getRealPathFromURI(uri) // Get the actual file path from the URI
        if (filePath != null) {
            val file = File(filePath) // Create a file object using the real path
            val requestBody =
                file.asRequestBody("image/*".toMediaTypeOrNull()) // Create request body

            // Create MultipartBody.Part for the image
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)

            // Convert taskId to RequestBody in the correct way for newer versions
            val taskIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), taskId.toString())

            // Make the network call to upload the image with task ID
            ImageClient.imageApi.uploadImage(imagePart, taskIdPart)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        if (response.isSuccessful) {
                            showToast("Image uploaded successfully!")
                            setResult(RESULT_OK) // Indicate success
                        } else {
                            showToast("Failed to upload image")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        showToast("Upload failed: ${t.message}")
                    }
                })
        } else {
            showToast("Failed to retrieve file path")
        }
    }

    // Function to retrieve the real file path from content URI
    private fun getRealPathFromURI(contentUri: Uri): String? {
        val cursor = contentResolver.query(
            contentUri,
            arrayOf(MediaStore.Images.Media.DATA),
            null,
            null,
            null
        )
        return cursor?.use {
            it.moveToFirst()
            it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
        }
    }

    // Custom Toast function
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
