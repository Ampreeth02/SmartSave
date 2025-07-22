package com.example.personalfinancetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnbordingActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onbording1)
        val skip: Button = findViewById(R.id.button)
        val Next: Button = findViewById(R.id.button2)


        skip.setOnClickListener {
            val intent = Intent(this, OnbordingActivity2::class.java)
            startActivity(intent)
        }


        Next.setOnClickListener {
            val intent = Intent(this, OnbordingActivity2::class.java)
            startActivity(intent)
        }

    }
}