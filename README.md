# Verita

A modern Matrix client for Android built with Jetpack Compose and Matrix Android SDK 2.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **SDK**: Matrix Android SDK 2 (`org.matrix.android:matrix-android-sdk2`)
- **DI**: Hilt (Dagger)
- **Image Loading**: Coil
- **Navigation**: Manual state-based navigation (supporting Compose Destinations annotations)
- **Localization**: Multi-language support (EN/RU) via custom `SettingsManager`

## Core Features
- **Authentication**: Login via username/password with support for custom homeservers.
- **Room List**: Real-time room summaries with last message previews and fallback demo data.
- **Chat**: 
    - Real-time messaging using Timeline API.
    - Support for text messages.
    - Reverse layout for natural chat flow.
- **Settings**: 
    - Proxy server configuration.
    - Custom RGB theme coloring.
    - Dynamic language switching.

## Project Structure
- `ui/login`: Authentication screens and `LoginViewModel`.
- `ui/roomlist`: Room list management and `RoomListViewModel`.
- `ui/chat`: Timeline-based chat interface and `ChatViewModel`.
- `ui/theme`: Material 3 theme configuration with dynamic color support.
- `data/`: `SettingsManager` for persistent configuration.
- `di/`: Hilt modules for Matrix SDK and app dependencies.

## Build Instructions
1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Sync Gradle projects.
4. Run `./gradlew :app:assembleDebug` or deploy via Android Studio.
