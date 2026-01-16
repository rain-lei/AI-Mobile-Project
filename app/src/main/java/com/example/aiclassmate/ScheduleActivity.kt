package com.example.aiclassmate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScheduleActivity : AppCompatActivity() {

    private lateinit var ivSchedule: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        ivSchedule = findViewById(R.id.ivSchedule)
        val btnUpload = findViewById<Button>(R.id.btnUploadSchedule)

        btnUpload.setOnClickListener {
            openFileChooser()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            ivSchedule.setImageURI(imageUri)
            Toast.makeText(this, "课表上传成功 (模拟)", Toast.LENGTH_SHORT).show()
            // In a real app, you would upload 'imageUri' to your server here
        }
    }
}
