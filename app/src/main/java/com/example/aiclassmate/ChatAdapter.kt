package com.example.aiclassmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutAi: LinearLayout = view.findViewById(R.id.layoutAi)
        val layoutUser: LinearLayout = view.findViewById(R.id.layoutUser)
        val tvAiContent: TextView = view.findViewById(R.id.tvAiContent)
        val tvUserContent: TextView = view.findViewById(R.id.tvUserContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_msg, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        
        if (message.isUser) {
            holder.layoutAi.visibility = View.GONE
            holder.layoutUser.visibility = View.VISIBLE
            holder.tvUserContent.text = message.content
        } else {
            holder.layoutAi.visibility = View.VISIBLE
            holder.layoutUser.visibility = View.GONE
            holder.tvAiContent.text = message.content
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
    
    fun removeLastMessage() {
        if (messages.isNotEmpty()) {
            messages.removeAt(messages.size - 1)
            notifyItemRemoved(messages.size)
        }
    }
}
