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
    val isLoading: Boolean = false, // Add loading state
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE = 1
        private const val VIEW_TYPE_LOADING = 2
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutAi: LinearLayout = view.findViewById(R.id.layoutAi)
        val layoutUser: LinearLayout = view.findViewById(R.id.layoutUser)
        val tvAiContent: TextView = view.findViewById(R.id.tvAiContent)
        val tvUserContent: TextView = view.findViewById(R.id.tvUserContent)
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_loading, parent, false)
            LoadingViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_msg, parent, false)
            ChatViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatViewHolder) {
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
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
    
    fun removeLoadingMessage() {
        if (messages.isNotEmpty() && messages.last().isLoading) {
            val index = messages.size - 1
            messages.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
