package com.example.aiclassmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnSchedule).setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        findViewById<Button>(R.id.btnOcr).setOnClickListener {
            startActivity(Intent(this, OcrActivity::class.java))
        }

        findViewById<Button>(R.id.btnNlp).setOnClickListener {
            startActivity(Intent(this, NlpActivity::class.java))
        }

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            startActivity(Intent(this, FileShareActivity::class.java))
        }
    }
}
