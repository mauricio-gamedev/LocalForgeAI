package com.localaiforge.app.data

data class Conversation(
    val id: Long,
    val projectId: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
