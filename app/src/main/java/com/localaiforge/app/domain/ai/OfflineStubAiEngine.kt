package com.localaiforge.app.domain.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Temporary V0.2 engine. It deliberately does not call any paid/cloud API.
 * A llama.cpp-backed implementation will replace this in a later milestone.
 */
class OfflineStubAiEngine : AiEngine {
    override val isModelLoaded: Boolean = false

    override suspend fun loadModel(modelPath: String) = Unit
    override suspend fun unloadModel() = Unit

    override fun stream(request: AiRequest): Flow<AiChunk> = flow {
        emit(
            AiChunk(
                text = "Persistent chat is working. Install a local model in a later milestone to enable AI responses.",
                isFinal = true
            )
        )
    }
}
