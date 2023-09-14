package com.bangkit.eadiv2.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.eadiv2.R
import com.bangkit.eadiv2.apihelper.ApiClient
import com.bangkit.eadiv2.databinding.ActivityAddPhotoBinding
import com.bangkit.eadiv2.ui.result.ResultActivity
import com.bangkit.eadiv2.utils.reduceImageSize
import com.bangkit.eadiv2.utils.rotateFile
import com.bangkit.eadiv2.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPhotoBinding
    private var getFile: File? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val CAMERA_X_RESULT = 200
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupClickListeners()
        showLoading(false)
    }

    private fun setToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener {
            startCameraX()
        }

        binding.btnGallery.setOnClickListener {
            startGallery()
        }

        binding.btnUpload.setOnClickListener {
            uploadImage()
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            myFile?.let { file ->
                rotateFile(file, isBackCamera)
                getFile = file
                binding.ivPreview.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private fun startGallery() {
        val chooser = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        val galleryIntent = Intent.createChooser(chooser, getString(R.string.choose_a_picture))
        launcherIntentGallery.launch(galleryIntent)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddPhotoActivity)
                getFile = myFile
                binding.ivPreview.setImageURI(uri)
            }
        }
    }

    private fun uploadImage() {
        val apiService = ApiClient.create()

        if (getFile != null) {
            val file = reduceImageSize(getFile as File)
            val reqImgFile =
                file.asRequestBody("image/*".toMediaTypeOrNull()) // Specify the media type as "image/jpeg"
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image", // Use the correct key as per API documentation
                file.name,
                reqImgFile
            )

            showLoading(true)

            apiService.uploadImage(imageMultipart).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val predictedLabel = response.body()
                        Log.d("UploadImage", "Predicted label: $predictedLabel")
                        if (!predictedLabel.isNullOrEmpty()) {
                            val intent = Intent(this@AddPhotoActivity, ResultActivity::class.java)
                            intent.putExtra("imagePath", file.path)
                            intent.putExtra("verdict", predictedLabel)

                            // Debugging: Log the file path
                            Log.d("AddPhotoActivity", "Image file path: ${file.path}")

                            startActivity(intent)
                            showToast(getString(R.string.upload_success))
                            finish() // Finish the AddPhotoActivity after starting ResultActivity
                        } else {
                            showToast(getString(R.string.empty_predicted_label))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("UploadImage", "Upload failed. Error: ${response.code()}, $errorBody")
                        showLoading(false)
                        showToast(getString(R.string.upload_failed))
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("UploadImage", "Upload failed. Error: ${t.message}")
                    showLoading(false)
                    showToast(getString(R.string.upload_failed))
                }
            })
        } else {
            showToast(getString(R.string.please_upload_a_picture_first))
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.failed_to_get_permission),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun showLoading(state: Boolean) {
        val progressBar = binding.progressBar
        if (state) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(text: String) {
        Toast.makeText(this@AddPhotoActivity, text, Toast.LENGTH_SHORT).show()
    }
}