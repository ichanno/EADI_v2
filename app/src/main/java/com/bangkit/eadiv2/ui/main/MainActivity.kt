package com.bangkit.eadiv2.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.eadiv2.databinding.ActivityMainBinding
import com.bangkit.eadiv2.ui.about.AboutActivity
import com.bangkit.eadiv2.ui.disclaimer.DisclaimerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivAbout.setOnClickListener { v: View? ->
            val intent = Intent(this@MainActivity, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.cvScan.setOnClickListener { v: View? ->
            val intent = Intent(this@MainActivity, DisclaimerActivity::class.java)
            startActivity(intent)
        }
    }
}