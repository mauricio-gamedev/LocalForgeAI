package com.localaiforge.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localaiforge.app.data.ChatMessage
import com.localaiforge.app.data.ChatRepository
import com.localaiforge.app.data.Conversation
import com.localaiforge.app.data.MessageRole
import com.localaiforge.app.data.Project
import com.localaiforge.app.data.ProjectRepository
import com.localaiforge.app.domain.ai.AiRequest
import com.localaiforge.app.domain.ai.OfflineStubAiEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(application)
    private val chatRepository = ChatRepository(application)
    private val aiEngine = OfflineStubAiEngine()

    val projects: StateFlow<List<Project>> = repository.projects
    val conversations: StateFlow<List<Conversation>> = chatRepository.conversations
    val messages: StateFlow<List<ChatMessage>> = chatRepository.messages

    private val _respondingConversationIds = MutableStateFlow<Set<Long>>(emptySet())
    val respondingConversationIds: StateFlow<Set<Long>> = _respondingConversationIds.asStateFlow()

    fun createProject(name: String, instructions: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createProject(name, instructions)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProject(project)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(id)
        }
    }

    fun createConversation(projectId: Long, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = chatRepository.createConversation(projectId)
            withContext(Dispatchers.Main) { onCreated(id) }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.deleteConversation(id)
        }
    }

    fun sendMessage(project: Project, conversationId: Long, text: String) {
        if (text.isBlank() || conversationId in _respondingConversationIds.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _respondingConversationIds.value += conversationId
            try {
                chatRepository.addMessage(conversationId, MessageRole.USER, text)

                val response = StringBuilder()
                aiEngine.stream(
                    AiRequest(
                        systemPrompt = project.instructions,
                        userMessage = text,
                        projectId = project.id
                    )
                ).collect { chunk ->
                    response.append(chunk.text)
                }

                if (response.isNotBlank()) {
                    chatRepository.addMessage(
                        conversationId,
                        MessageRole.ASSISTANT,
                        response.toString()
                    )
                }
            } finally {
                _respondingConversationIds.value -= conversationId
            }
        }
    }
}
