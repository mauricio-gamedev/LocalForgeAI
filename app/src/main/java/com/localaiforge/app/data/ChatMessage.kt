package com.localaiforge.app.data

enum class MessageRole(val dbValue: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    companion object {
        fun fromDb(value: String): MessageRole = entries.firstOrNull { it.dbValue == value } ?: ASSISTANT
    }
}

data class ChatMessage(
    val id: Long,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: Long
)
