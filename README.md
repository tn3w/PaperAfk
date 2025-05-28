# PaperAfk

A Minecraft plugin that allows players to effortlessly teleport to a designated AFK room in a separate world, ensuring they can take breaks without disrupting gameplay. 

## Features
- **Individual Rooms**: Each player gets their own personal AFK room in a separate world
- **Music System**: Built-in jukebox GUI allowing players to play any music disc with saved preferences
- **Admin Control**: Staff can toggle AFK status on other players with permission-based commands
- **Protected Environment**: AFK rooms are fully protected with no damage, block breaking, or item interactions
- **Auto-AFK**: Players are automatically marked as AFK after a configurable amount of time of inactivity

## Requirements

- Paper Minecraft server (version 1.21.4)
- Java 17 or higher

## Setup

1. Clone this repository
2. Build the plugin with Gradle:
   ```bash
   ./gradlew clean jar --no-daemon
   ```
3. Copy the built JAR (`build/libs/PaperAfk-<VERSION>.jar`) to your server's `plugins` folder
4. Start the server
5. Configure the plugin by editing `plugins/PaperAfk/config.yml`

## Formatting

1. Download the latest Google Java formatter jar from the [releases page](https://github.com/google/google-java-format/releases/latest)
2. Format all Java files:
   ```bash
   find src -name "*.java" | xargs java -jar google-java-format.jar --aosp --replace
   ```

## Configuration

```yaml
# PaperAfk Configuration

# World settings
afk-world-name: "afk_world"  # Name of the separate world where AFK rooms are created

# Room settings
room-size: 10                # Size of each player's AFK room in blocks (10 = 10x10)
room-distance: 100           # Distance between player AFK rooms to prevent overlap

# Permission settings
require-afk-permission: false      # Whether players need 'paperafk.afk' permission to use /afk
require-afk-other-permission: true # Whether staff need 'paperafk.afk.other' permission to toggle AFK on other players 

# Display settings
afk-indicator: "zZ"          # Text to display next to AFK players in the tab list 

# Auto-AFK settings
auto-afk:
  enabled: true              # Whether players are automatically marked as AFK after inactivity
  time-minutes: 5            # Time in minutes before a player is automatically marked as AFK
  show-overlay: true         # Whether to show an overlay on the screen when auto-AFK is triggered 
```

## Commands

- `/afk` - Teleports you to your personal AFK room. Use the same command to return to your previous location.
- `/afk <player>` - Toggles AFK status for another player (requires permission `paperafk.afk.other`).

## Permissions

- `paperafk.afk` - Allows use of the `/afk` command (only if `require-afk-permission` is enabled in config)
- `paperafk.afk.other` - Allows staff to toggle AFK status for other players

## Music System

- Click on a jukebox in your AFK room to open the music selection GUI
- Choose any music disc to play, and your selection will be remembered for future visits
- Use the stop button (red barrier) to stop the current music

## License
Copyright 2025 TN3W

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.