package com.localaiforge.app.domain.builder

/**
 * Contract for the future conversational app-builder.
 * The UI and AI model stay decoupled from file generation and build tooling.
 */
interface AppBuilder {
    suspend fun createWorkspace(spec: AppSpec): Workspace
    suspend fun applyChange(workspace: Workspace, request: String): WorkspaceChange
}

data class AppSpec(
    val name: String,
    val platform: TargetPlatform,
    val description: String
)

enum class TargetPlatform {
    ANDROID,
    WEB
}

data class Workspace(
    val id: String,
    val displayName: String,
    val rootPath: String
)

data class WorkspaceChange(
    val summary: String,
    val changedFiles: List<String>,
    val success: Boolean
)
