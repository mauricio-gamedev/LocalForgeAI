package com.localaiforge.app.data

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProjectRepository(context: Context) {
    private val dbHelper = ProjectDbHelper(context.applicationContext)
    private val _projects = MutableStateFlow(loadProjects())

    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    @Synchronized
    fun createProject(name: String, instructions: String = ""): Long {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Project name cannot be empty" }

        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put("name", cleanName)
            put("instructions", instructions.trim())
            put("created_at", now)
            put("updated_at", now)
        }

        val id = dbHelper.writableDatabase.insertOrThrow("projects", null, values)
        refresh()
        return id
    }

    @Synchronized
    fun updateProject(project: Project) {
        val values = ContentValues().apply {
            put("name", project.name.trim())
            put("instructions", project.instructions.trim())
            put("updated_at", System.currentTimeMillis())
        }

        dbHelper.writableDatabase.update(
            "projects",
            values,
            "id = ?",
            arrayOf(project.id.toString())
        )
        refresh()
    }

    @Synchronized
    fun deleteProject(id: Long) {
        dbHelper.writableDatabase.delete("projects", "id = ?", arrayOf(id.toString()))
        refresh()
    }

    fun getProject(id: Long): Project? = loadProjects().firstOrNull { it.id == id }

    private fun refresh() {
        _projects.value = loadProjects()
    }

    private fun loadProjects(): List<Project> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "projects",
            arrayOf("id", "name", "instructions", "created_at", "updated_at"),
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
                        Project(
                            id = it.getLong(it.getColumnIndexOrThrow("id")),
                            name = it.getString(it.getColumnIndexOrThrow("name")),
                            instructions = it.getString(it.getColumnIndexOrThrow("instructions")),
                            createdAt = it.getLong(it.getColumnIndexOrThrow("created_at")),
                            updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))
                        )
                    )
                }
            }
        }
    }
}
