package com.bangkit.eadiv2.ui.instruction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.bangkit.eadiv2.R
import com.bangkit.eadiv2.databinding.ActivityInstructionBinding
import com.bangkit.eadiv2.databinding.ActivityPhotoInstructionBinding
import com.bangkit.eadiv2.ui.camera.AddPhotoActivity

class PhotoInstructionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoInstructionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoInstructionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()

        binding.btnContinue.setOnClickListener { v: View? ->
            val intent = Intent(this@PhotoInstructionActivity, AddPhotoActivity::class.java)
            startActivity(intent)
        }
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
}