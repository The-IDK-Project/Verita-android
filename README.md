                            Verita

A modern Matrix client for Android built with Jetpack Compose and Matrix Android SDK 2.

                            Screenshots
<img width="576" height="1280" alt="photo_2026-05-03_22-59-46" src="https://github.com/user-attachments/assets/4f118c98-4d2a-415a-af78-785299f9524d" /> <img width="576" height="1280" alt="photo_2026-05-03_22-59-48" src="https://github.com/user-attachments/assets/0144ceb0-47ae-43dc-b489-2ca299c92fa0" /> <img width="576" height="1280" alt="photo_2026-05-03_22-59-50" src="https://github.com/user-attachments/assets/e60d7786-78bd-4266-823a-de6265905cf0" /> <img width="576" height="1280" alt="photo_2026-05-03_22-59-53" src="https://github.com/user-attachments/assets/e6a32177-8b99-4605-ab3f-21ef5aec2b08" />


## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **SDK**: Matrix Android SDK 2 (`org.matrix.android:matrix-android-sdk2`)
- **DI**: Hilt (Dagger)
- **Image Loading**: Coil
- **Navigation**: Manual state-based navigation (supporting Compose Destinations annotations)
- **Localization**: Multi-language support (EN/RU) via custom `SettingsManager`

## Core Features
- **Authentication**: 
    - Login via username/password with support for custom homeservers.
    - **Telegram Integration**: Support for logging in via Matrix-Telegram bridge using:
        - Phone number + SMS/2FA.
        - Email-based authentication.
        - Official Telegram Web Widget (WebView intercept).
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
