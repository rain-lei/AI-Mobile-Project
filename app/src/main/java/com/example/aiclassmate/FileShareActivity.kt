package com.example.aiclassmate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FileShareActivity : AppCompatActivity() {

    private val PICK_FILE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fileshare)

        findViewById<Button>(R.id.btnSelectFile).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Allow all file types
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri: Uri? = data.data
            fileUri?.let {
                val msg = getString(R.string.msg_file_uploading, it.lastPathSegment ?: "unknown")
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
