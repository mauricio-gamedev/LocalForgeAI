package com.localaiforge.app.domain.ai

import kotlinx.coroutines.flow.Flow

data class AiRequest(
    val systemPrompt: String,
    val userMessage: String,
    val projectId: Long? = null
)

data class AiChunk(
    val text: String,
    val isFinal: Boolean = false
)

interface AiEngine {
    val isModelLoaded: Boolean

    suspend fun loadModel(modelPath: String)
    suspend fun unloadModel()
    fun stream(request: AiRequest): Flow<AiChunk>
}
