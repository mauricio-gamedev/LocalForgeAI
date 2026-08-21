# Changelog

## 0.2.0
- Added persistent conversations per project.
- Added persistent local chat messages in SQLite.
- Added automatic chat titles from the first user message.
- Added conversation switching and New chat flow.
- Wired the offline `AiEngine` placeholder into the chat pipeline.
- Added safe database migration from schema 1 to schema 2.
- Updated Compose build to compileSdk 37 for Compose BOM 2026.08.00.
- Kept the app fully offline with no paid API dependency.

## 0.1.0
- Initial project foundation.
- Local project database.
- Project list/detail UI.
- `AiEngine` abstraction and offline stub.
- `AppBuilder` contract.
- GitHub Actions Android build workflow.
