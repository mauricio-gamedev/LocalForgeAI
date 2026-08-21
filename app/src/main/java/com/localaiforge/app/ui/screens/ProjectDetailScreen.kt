package com.localaiforge.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localaiforge.app.data.ChatMessage
import com.localaiforge.app.data.Conversation
import com.localaiforge.app.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: Project,
    conversations: List<Conversation>,
    messages: List<ChatMessage>,
    respondingConversationIds: Set<Long>,
    onCreateConversation: ((Long) -> Unit) -> Unit,
    onSendMessage: (Long, String) -> Unit,
    onBack: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var currentConversationId by remember(project.id) { mutableStateOf<Long?>(null) }
    val projectConversations = conversations.filter { it.projectId == project.id }
    val tabs = listOf("Chat", "Files", "Memory", "Build")

    LaunchedEffect(projectConversations) {
        if (currentConversationId == null || projectConversations.none { it.id == currentConversationId }) {
            currentConversationId = projectConversations.firstOrNull()?.id
        }
        if (projectConversations.isEmpty()) {
            onCreateConversation { currentConversationId = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> ChatScreen(
                        conversations = projectConversations,
                        messages = messages,
                        currentConversationId = currentConversationId,
                        isResponding = currentConversationId?.let { it in respondingConversationIds } == true,
                        onSelectConversation = { currentConversationId = it },
                        onNewConversation = {
                            onCreateConversation { currentConversationId = it }
                        },
                        onSendMessage = { text ->
                            currentConversationId?.let { onSendMessage(it, text) }
                        }
                    )
                    1 -> PlaceholderCard(
                        title = "Project files",
                        body = "Source code, documents and imported files will attach to this project in a later milestone."
                    )
                    2 -> PlaceholderCard(
                        title = "Project memory",
                        body = "Facts, decisions and summaries will stay isolated per project so the AI can recover context efficiently."
                    )
                    3 -> {
                        PlaceholderCard(
                            title = "AI App Builder",
                            body = "The workspace contract is already in place. Future versions will generate source files, validate builds and export APKs."
                        )
                        Button(onClick = { }, enabled = false) {
                            Text("Builder comes in a later milestone")
                        }
                    }
                }

                if (tab != 0 && project.instructions.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Project instructions", style = MaterialTheme.typography.titleSmall)
                    Text(project.instructions, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun PlaceholderCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
