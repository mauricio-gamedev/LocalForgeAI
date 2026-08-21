package com.localaiforge.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localaiforge.app.data.ChatMessage
import com.localaiforge.app.data.Conversation
import com.localaiforge.app.data.MessageRole

@Composable
fun ChatScreen(
    conversations: List<Conversation>,
    messages: List<ChatMessage>,
    currentConversationId: Long?,
    isResponding: Boolean,
    onSelectConversation: (Long) -> Unit,
    onNewConversation: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val currentConversation = conversations.firstOrNull { it.id == currentConversationId }
    val currentMessages = messages.filter { it.conversationId == currentConversationId }
    val listState = rememberLazyListState()

    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Conversation", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { menuExpanded = true }) {
                    Text(currentConversation?.title ?: "Select chat")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    conversations.forEach { conversation ->
                        DropdownMenuItem(
                            text = { Text(conversation.title, maxLines = 1) },
                            onClick = {
                                menuExpanded = false
                                onSelectConversation(conversation.id)
                            }
                        )
                    }
                }
            }
            Button(onClick = onNewConversation) {
                Text("New chat")
            }
        }

        if (currentMessages.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Persistent chat is ready", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Messages are stored locally inside this project. Until the local model is installed, the assistant uses the offline V0.2 placeholder engine."
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentMessages, key = { it.id }) { message ->
                MessageCard(message)
            }
            if (isResponding) {
                item {
                    Text("Local engine is responding…", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Message") },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 5,
                enabled = currentConversationId != null && !isResponding
            )
            Button(
                enabled = draft.isNotBlank() && currentConversationId != null && !isResponding,
                onClick = {
                    val message = draft
                    draft = ""
                    onSendMessage(message)
                }
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun MessageCard(message: ChatMessage) {
    val author = when (message.role) {
        MessageRole.USER -> "You"
        MessageRole.ASSISTANT -> "LocalForge"
        MessageRole.SYSTEM -> "System"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(author, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(message.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
