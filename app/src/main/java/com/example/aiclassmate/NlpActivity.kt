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
                // 显示我发送的内容
                appendChat(getString(R.string.nlp_user_prefix, keyword))
                etInput.text.clear()
                getAiResponse(keyword)
            }
        }
    }

    private fun appendChat(text: String) {
        val currentText = tvChatHistory.text.toString()
        tvChatHistory.text = "$currentText\n\n$text"
    }

    private fun getAiResponse(keyword: String) {
        // 模拟 AI 回复
        // 在真实项目中，这里会调用 Retrofit 接口请求后端
        val mockResponse = getString(R.string.nlp_mock_response, keyword)
        appendChat(mockResponse)
    }
}
