package com.bangkit.eadiv2.ui.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.eadiv2.databinding.ActivityResultBinding
import java.io.File

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the image path and verdict from the intent
        val imagePath = intent.getStringExtra("imagePath") ?: ""
        val verdict = intent.getStringExtra("verdict")

        // Convert the file path to a content URI
        val contentUri = Uri.fromFile(File(imagePath))

        // Load and display the image
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
            binding.ivImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set the verdict text
        binding.tvVerdict.text = verdict
    }
}
