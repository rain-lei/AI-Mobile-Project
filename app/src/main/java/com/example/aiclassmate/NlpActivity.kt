package com.example.aiclassmate

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aiclassmate.network.ChatRequest
import com.example.aiclassmate.network.ChatResponse
import com.example.aiclassmate.network.ConversationApiService
import com.example.aiclassmate.network.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NlpActivity : AppCompatActivity() {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var etApiKey: EditText
    private lateinit var cardSettings: CardView
    private lateinit var radioGroupProvider: RadioGroup
    
    // UI controls
    private lateinit var btnSend: Button
    private lateinit var btnSettings: View
    private lateinit var btnSaveConfig: Button

    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    // Config data
    private var currentApiKey: String = ""
    private var currentBaseUrl: String = "https://api.deepseek.com/" // Default
    private var currentModel: String = "deepseek-chat" // Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nlp)

        // Init Views
        recyclerViewChat = findViewById(R.id.recyclerViewChat)
        etInput = findViewById(R.id.etInput)
        etApiKey = findViewById(R.id.etApiKey)
        cardSettings = findViewById(R.id.cardSettings)
        radioGroupProvider = findViewById(R.id.radioGroupProvider)
        btnSend = findViewById(R.id.btnSend)
        btnSettings = findViewById(R.id.btnSettings)
        btnSaveConfig = findViewById(R.id.btnSaveConfig)

        // Init RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        recyclerViewChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerViewChat.adapter = chatAdapter

        // Load saved config
        loadConfig()

        // Set Listeners
        btnSettings.setOnClickListener {
            if (cardSettings.visibility == View.VISIBLE) {
                cardSettings.visibility = View.GONE
            } else {
                cardSettings.visibility = View.VISIBLE
            }
        }

        btnSaveConfig.setOnClickListener {
            saveConfig()
            cardSettings.visibility = View.GONE
            Toast.makeText(this, getString(R.string.nlp_config_saved), Toast.LENGTH_SHORT).show()
        }

        btnSend.setOnClickListener {
            val question = etInput.text.toString().trim()
            if (question.isNotEmpty()) {
                if (currentApiKey.isEmpty()) {
                    Toast.makeText(this, getString(R.string.nlp_error_no_key), Toast.LENGTH_LONG).show()
                    cardSettings.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                
                // Show user message
                addMessageToChat(question, true)
                etInput.text.clear()
                
                // Call API
                performApiCall(question)
            }
        }
    }

    private fun loadConfig() {
        val sharedPrefs = getSharedPreferences("AI_CONFIG", Context.MODE_PRIVATE)
        currentApiKey = sharedPrefs.getString("API_KEY", "") ?: ""
        currentBaseUrl = sharedPrefs.getString("BASE_URL", "https://api.deepseek.com/") ?: "https://api.deepseek.com/"
        currentModel = sharedPrefs.getString("MODEL_NAME", "deepseek-chat") ?: "deepseek-chat"
        
        etApiKey.setText(currentApiKey)
        
        // Restore Radio Selection
        when (currentModel) {
            "moonshot-v1-8k" -> radioGroupProvider.check(R.id.rbKimi)
            "gpt-3.5-turbo" -> radioGroupProvider.check(R.id.rbOpenAI)
            else -> radioGroupProvider.check(R.id.rbDeepSeek)
        }
    }

    private fun saveConfig() {
        currentApiKey = etApiKey.text.toString().trim()
        
        when (radioGroupProvider.checkedRadioButtonId) {
            R.id.rbDeepSeek -> {
                // Check if it's an OpenRouter key
                if (currentApiKey.startsWith("sk-or-")) {
                    currentBaseUrl = "https://openrouter.ai/api/v1/"
                    currentModel = "deepseek/deepseek-chat"
                } else {
                    currentBaseUrl = "https://api.deepseek.com/"
                    currentModel = "deepseek-chat"
                }
            }
            R.id.rbKimi -> {
                currentBaseUrl = "https://api.moonshot.cn/v1/"
                currentModel = "moonshot-v1-8k"
            }
            R.id.rbOpenAI -> {
                currentBaseUrl = "https://api.openai.com/v1/"
                currentModel = "gpt-3.5-turbo"
            }
        }

        val sharedPrefs = getSharedPreferences("AI_CONFIG", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("API_KEY", currentApiKey)
            putString("BASE_URL", currentBaseUrl)
            putString("MODEL_NAME", currentModel)
            apply()
        }
    }

    private fun performApiCall(question: String) {
        addMessageToChat("AI 正在思考...", false)

        // Build Retrofit Client
        val retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ConversationApiService::class.java)

        // Prepare Request
        // DeepSeek 和 Kimi 兼容 OpenAI 格式
        val messages = listOf(
            Message("system", "你是一个热心的辅导老师，请用中文简单明了地回答学生的问题。如果涉及公式请尽量用Latex格式。"),
            Message("user", question)
        )
        val request = ChatRequest(model = currentModel, messages = messages)

        // Execute Call
        service.sendMessage("Bearer $currentApiKey", request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                chatAdapter.removeLastMessage() // Remove "Thinking..."
                
                if (response.isSuccessful && response.body() != null) {
                    val reply = response.body()!!.choices.firstOrNull()?.message?.content ?: "空响应"
                    addMessageToChat(reply, false)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "未知错误"
                    addMessageToChat("错误: $errorMsg", false)
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                chatAdapter.removeLastMessage()
                addMessageToChat("网络请求失败: ${t.message}", false)
            }
        })
    }

    private fun addMessageToChat(text: String, isUser: Boolean) {
        val msg = ChatMessage(text, isUser)
        chatAdapter.addMessage(msg)
        if (chatMessages.isNotEmpty()) {
            recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
        }
    }
}
