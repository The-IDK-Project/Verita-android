# Changelog

All notable changes to this project will be documented in this file.

## [0.0.0.4-alpha] - 2024-05-24

### Added
- **Social Login & Rich Presence (Draft)**:
    - Added UI buttons for Discord and other social authentication flows.
    - Created a placeholder for cross-platform status synchronization.
- **Telegram Login Integration**: 
    - Manual entry for Phone Number and SMS activation codes.
    - Support for Email-based authentication steps in the Telegram UI.
    - `TelegramWebViewLogin` component for the official Telegram Web Widget.
    - URL interception in WebView to capture `tgauth://` data and `access_token` fragments.
- **UI Enhancements**:
    - Branded "LOGIN WITH TELEGRAM" button in the main login screen.
    - Smooth transitions between manual Telegram entry and the Web Widget flow.
    - Language selector in the login screen (English/Russian).

### Changed
- **Authentication Logic**:
    - Improved Matrix ID sanitization for the Telegram bridge (handling symbols like `+`, `@`, and `.`).
    - Password hashing for bridge-specific credentials in manual flow.
- **Documentation**: Updated `README.md` with new authentication capabilities.

### Fixed
- Experimental Material3 API usage in `TelegramWebViewLogin`.
- WebView configuration to support JavaScript and DOM storage required by the Telegram widget.
