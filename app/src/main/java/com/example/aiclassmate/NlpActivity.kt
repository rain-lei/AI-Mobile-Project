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

import android.view.inputmethod.InputMethodManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NlpActivity : AppCompatActivity() {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etInput: EditText
    private lateinit var etApiKey: EditText
    private lateinit var cardSettings: CardView
    private lateinit var radioGroupProvider: RadioGroup
    private lateinit var viewStatus: View // Connection status
    
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

    // Single Retrofit instance to reuse connection pool
    private var currentRetrofit: Retrofit? = null

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
        viewStatus = findViewById(R.id.viewStatus)

        // Init RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        recyclerViewChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerViewChat.adapter = chatAdapter

        // Load saved config
        loadConfig()
        
        // Add Welcome Message
        if (chatMessages.isEmpty()) {
            addMessageToChat("你好！我是你的 AI 课代表。我可以帮你解答问题、翻译文本或提供学习建议。请先在设置中配置 API Key 哦！", false)
        }

        // Set Listeners
        btnSettings.setOnClickListener {
            toggleSettings()
        }

        btnSaveConfig.setOnClickListener {
            saveConfig()
            toggleSettings()
            Toast.makeText(this, getString(R.string.nlp_config_saved), Toast.LENGTH_SHORT).show()
        }

        btnSend.setOnClickListener {
            val question = etInput.text.toString().trim()
            if (question.isNotEmpty()) {
                if (currentApiKey.isEmpty()) {
                    Toast.makeText(this, getString(R.string.nlp_error_no_key), Toast.LENGTH_LONG).show()
                    toggleSettings()
                    return@setOnClickListener
                }
                
                // Show user message
                addMessageToChat(question, true)
                etInput.text.clear()
                hideKeyboard()
                
                // Call API
                performApiCall(question)
            }
        }
    }

    private fun toggleSettings() {
        if (cardSettings.visibility == View.VISIBLE) {
            cardSettings.visibility = View.GONE
        } else {
            cardSettings.visibility = View.VISIBLE
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
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
        
        updateRetrofitClient()
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
        
        updateRetrofitClient()
    }
    
    private fun updateRetrofitClient() {
        // Create custom OkHttpClient with timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
            
        currentRetrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        // Visual indicator update (simple logic)
        viewStatus.visibility = if (currentApiKey.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun performApiCall(question: String) {
        // Add loading indicator
        chatAdapter.addMessage(ChatMessage("", false, true))
        recyclerViewChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

        val service = currentRetrofit?.create(ConversationApiService::class.java) ?: return

        // Context-aware prompt
        val messages = listOf(
            Message("system", "你是一个专业、亲切的AI辅导老师。请用Markdown格式清晰地回答问题，公式使用Latex。"),
            Message("user", question)
        )
        val request = ChatRequest(model = currentModel, messages = messages)

        service.sendMessage("Bearer $currentApiKey", request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                chatAdapter.removeLoadingMessage()
                
                if (response.isSuccessful && response.body() != null) {
                    val choice = response.body()!!.choices.firstOrNull()
                    val reply = choice?.message?.content ?: "（AI 返回了空内容）"
                    addMessageToChat(reply, false)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = if (!errorBody.isNullOrEmpty()) {
                        // Try to parse JSON error if possible, otherwise simple text
                        if (errorBody.contains("error")) "API 错误: $errorBody" else "请求被拒绝: ${response.code()}"
                    } else {
                        "未知错误: ${response.code()}"
                    }
                    addMessageToChat(errorMsg, false)
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                chatAdapter.removeLoadingMessage()
                addMessageToChat("网络连接失败，请检查网络设置。\n错误详情: ${t.localizedMessage}", false)
            }
        })
    }

    private fun addMessageToChat(text: String, isUser: Boolean) {
        val msg = ChatMessage(text, isUser)
        chatAdapter.addMessage(msg)
        recyclerViewChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }
}
