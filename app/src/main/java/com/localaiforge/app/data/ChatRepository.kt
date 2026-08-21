package com.localaiforge.app.data

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(context: Context) {
    private val dbHelper = ProjectDbHelper(context.applicationContext)
    private val _conversations = MutableStateFlow(loadConversations())
    private val _messages = MutableStateFlow(loadMessages())

    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    @Synchronized
    fun createConversation(projectId: Long, title: String = "New chat"): Long {
        val now = System.currentTimeMillis()
        val id = dbHelper.writableDatabase.insertOrThrow(
            "conversations",
            null,
            ContentValues().apply {
                put("project_id", projectId)
                put("title", title.trim().ifBlank { "New chat" })
                put("created_at", now)
                put("updated_at", now)
            }
        )
        refreshConversations()
        return id
    }

    @Synchronized
    fun addMessage(conversationId: Long, role: MessageRole, content: String): Long {
        val cleanContent = content.trim()
        require(cleanContent.isNotEmpty()) { "Message cannot be empty" }

        val now = System.currentTimeMillis()
        val db = dbHelper.writableDatabase
        val id = db.insertOrThrow(
            "messages",
            null,
            ContentValues().apply {
                put("conversation_id", conversationId)
                put("role", role.dbValue)
                put("content", cleanContent)
                put("created_at", now)
            }
        )
        db.update(
            "conversations",
            ContentValues().apply { put("updated_at", now) },
            "id = ?",
            arrayOf(conversationId.toString())
        )

        if (role == MessageRole.USER) {
            maybeSetConversationTitle(conversationId, cleanContent)
        }

        refreshAll()
        return id
    }

    @Synchronized
    fun deleteConversation(id: Long) {
        dbHelper.writableDatabase.delete("conversations", "id = ?", arrayOf(id.toString()))
        refreshAll()
    }

    private fun maybeSetConversationTitle(conversationId: Long, firstUserMessage: String) {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            "conversations",
            arrayOf("title"),
            "id = ?",
            arrayOf(conversationId.toString()),
            null,
            null,
            null
        )
        val currentTitle = cursor.use {
            if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow("title")) else null
        }

        if (currentTitle == "New chat") {
            val generatedTitle = firstUserMessage
                .lineSequence()
                .firstOrNull()
                .orEmpty()
                .take(48)
                .ifBlank { "New chat" }
            db.update(
                "conversations",
                ContentValues().apply { put("title", generatedTitle) },
                "id = ?",
                arrayOf(conversationId.toString())
            )
        }
    }

    private fun refreshAll() {
        refreshConversations()
        _messages.value = loadMessages()
    }

    private fun refreshConversations() {
        _conversations.value = loadConversations()
    }

    private fun loadConversations(): List<Conversation> {
        val cursor = dbHelper.readableDatabase.query(
            "conversations",
            arrayOf("id", "project_id", "title", "created_at", "updated_at"),
            null,
            null,
            null,
            null,
            "updated_at DESC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        Conversation(
                            id = it.getLong(it.getColumnIndexOrThrow("id")),
                            projectId = it.getLong(it.getColumnIndexOrThrow("project_id")),
                            title = it.getString(it.getColumnIndexOrThrow("title")),
                            createdAt = it.getLong(it.getColumnIndexOrThrow("created_at")),
                            updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))
                        )
                    )
                }
            }
        }
    }

    private fun loadMessages(): List<ChatMessage> {
        val cursor = dbHelper.readableDatabase.query(
            "messages",
            arrayOf("id", "conversation_id", "role", "content", "created_at"),
            null,
            null,
            null,
            null,
            "created_at ASC, id ASC"
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(
                        ChatMessage(
                            id = it.getLong(it.getColumnIndexOrThrow("id")),
                            conversationId = it.getLong(it.getColumnIndexOrThrow("conversation_id")),
                            role = MessageRole.fromDb(it.getString(it.getColumnIndexOrThrow("role"))),
                            content = it.getString(it.getColumnIndexOrThrow("content")),
                            createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                        )
                    )
                }
            }
        }
    }
}
