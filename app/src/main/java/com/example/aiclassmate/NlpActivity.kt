package com.example.aiclassmate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NlpActivity : AppCompatActivity() {

    private lateinit var tvChatHistory: TextView
    private lateinit var etInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nlp)

        tvChatHistory = findViewById(R.id.tvChatHistory)
        etInput = findViewById(R.id.etInput)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {
            val keyword = etInput.text.toString()
            if (keyword.isNotEmpty()) {
                appendChat("Me: $keyword")
                etInput.text.clear()
                getAiResponse(keyword)
            }
        }
    }

    private fun appendChat(text: String) {
        val currentText = tvChatHistory.text.toString()
        tvChatHistory.text = "$currentText\n$text"
    }

    private fun getAiResponse(keyword: String) {
        // Normally you would make a Retrofit call here to an LLM API (like GPT, Claude, or local model)
        // Mocking the response for now.
        
        val mockResponse = "AI: 针对关键词 '$keyword'，相关的知识点解析如下：\n" +
                "1. 定义：XXXX\n" +
                "2. 核心公式：E=mc^2\n" +
                "3. 常见考点：..." 
        
        appendChat(mockResponse)
    }
}
