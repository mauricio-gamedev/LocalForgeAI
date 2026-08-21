# LocalForge AI

LocalForge AI is an experimental Android-first local AI workspace designed around persistent projects, local memory, files, and a future conversational app builder.

## Core rule

The default architecture must remain usable without a paid API or mandatory server. Local inference is the primary path.

## Current milestone — V0.2

- Create and persist projects locally.
- Add project-specific instructions.
- Create multiple persistent conversations per project.
- Persist user/assistant messages in SQLite.
- Automatically title chats from the first user message.
- Switch between conversations and start a new chat.
- Keep the AI engine behind a replaceable `AiEngine` interface.
- Keep app-generation tooling behind a replaceable `AppBuilder` interface.
- Build Android APKs with GitHub Actions.

The current assistant response is intentionally an offline placeholder. A later milestone will connect a local GGUF model through a llama.cpp-backed engine.

## Planned direction

1. V0.3 — project memory and automatic summaries.
2. V0.4 — local GGUF inference.
3. V0.5 — project files and local retrieval/RAG.
4. V0.6 — conversational source-file generation.
5. V0.7 — build, diagnose, patch, rebuild, and export APK workflows.

## Android toolchain

- Kotlin 2.3.21
- Android Gradle Plugin 9.3.0
- Gradle 9.5
- JDK 17
- Jetpack Compose BOM 2026.08.00
- compileSdk 37
- targetSdk 36
- minSdk 33

## Data model

All project/chat data is stored locally in SQLite. Schema migrations must preserve user data; destructive upgrades are not allowed by default.

## Package

`com.localaiforge.app`

The package and product name are still provisional while the architecture is being established.
