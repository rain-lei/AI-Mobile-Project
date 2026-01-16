package com.example.aiclassmate.network

import com.google.gson.annotations.SerializedName

// 通用的 OpenAI 格式请求体
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String, // "user" or "assistant" or "system"
    val content: String
)

// 通用的响应体
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
