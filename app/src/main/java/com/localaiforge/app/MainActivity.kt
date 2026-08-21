package com.localaiforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localaiforge.app.data.Project
import com.localaiforge.app.ui.screens.ProjectDetailScreen
import com.localaiforge.app.ui.screens.ProjectsScreen
import com.localaiforge.app.ui.theme.LocalForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalForgeTheme {
                LocalForgeApp()
            }
        }
    }
}

@Composable
private fun LocalForgeApp(projectViewModel: ProjectViewModel = viewModel()) {
    val projects by projectViewModel.projects.collectAsState()
    val conversations by projectViewModel.conversations.collectAsState()
    val messages by projectViewModel.messages.collectAsState()
    val respondingConversationIds by projectViewModel.respondingConversationIds.collectAsState()
    var openedProjectId by remember { mutableStateOf<Long?>(null) }

    val openedProject: Project? = projects.firstOrNull { it.id == openedProjectId }

    if (openedProject == null) {
        ProjectsScreen(
            projects = projects,
            onCreateProject = projectViewModel::createProject,
            onOpenProject = { openedProjectId = it.id }
        )
    } else {
        ProjectDetailScreen(
            project = openedProject,
            conversations = conversations,
            messages = messages,
            respondingConversationIds = respondingConversationIds,
            onCreateConversation = { callback ->
                projectViewModel.createConversation(openedProject.id, callback)
            },
            onSendMessage = { conversationId, text ->
                projectViewModel.sendMessage(openedProject, conversationId, text)
            },
            onBack = { openedProjectId = null }
        )
    }
}
