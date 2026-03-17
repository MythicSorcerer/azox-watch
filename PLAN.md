# AzoxWatch Development Plan

## 1. Project Initialization
- [x] Rename artifact and project name in `pom.xml` to `azox-watch`.
- [x] Rename main class `AzoxTemplate` to `AzoxWatch`.
- [x] Update `paper-plugin.yml` with new main class and command definitions.
- [x] Update `config.yml` with initial settings, blacklist, and obfuscated bypass code.

## 2. Core Managers
- [x] **ConfigManager**: 
    - Handle loading and saving of `config.yml`.
    - Implement logic to decode the "obfuscated" bypass code (e.g., Base64 or a simple shift).
- [x] **LogManager**:
    - Handle creation and writing to player-specific log files in `plugins/AzoxWatch/logs/`.
    - Format: `username_uuid.log`.
    - Implement thread-safe asynchronous logging to avoid lagging the main server thread.
- [x] **BypassManager**:
    - Store a set of UUIDs that have successfully bypassed command restrictions for the current session.

## 3. Command Implementation
- [x] **AzCommand**:
    - `/az info`: Display version and plugin information.
    - `/az <4-digit-code>`: Verify the code against the one in config and grant bypass status.
    - Implement `TabCompleter` to only show `info` and not the code.

## 4. Listeners & Logging
- [x] **ConnectionListener**:
    - Log Login: `[L] [HH:MM:SS] Login [Coordinates: x y z] [IP: Public IP]`.
    - Log Disconnect: `[L] [HH:MM:SS] Disconnected [Coordinates: x y z] [IP: Public IP]`.
    - Reset bypass status on disconnect.
- [x] **CommandListener**:
    - Log all commands: `[C] [HH:MM:SS] /command`.
    - Block blacklisted commands if the player hasn't bypassed.
- [x] **GameModeListener**:
    - Log game mode changes: `[G] [HH:MM:SS] oldMode -> newMode [Reason]`.
    - Detect reasons like `/gm s` or `F3-N`/`F3-F4`.
- [x] **CreativeListener**:
    - Log items taken from creative inventory.
    - Format: `[T] [HH:MM:SS]: - Taken: ITEM - Amount: X - NBT: ...`.
    - Skip NBT if empty.

## 5. Finalization
- [x] Ensure all code follows the requested standards (Lombok, fully qualified names, `this` keyword, `final`).
- [x] Verify null safety and error handling.
- [x] Test all logging formats and bypass logic.
