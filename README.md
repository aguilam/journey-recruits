# Journey Recruits

**Short notice:** This mod was "vibe-coded" for personal use and will most likely **not** be actively supported going forward. Use at your own risk.

## Overview

RecruitBridge integrates claims from the **Recruits** mod into **JourneyMap** overlays.

## Features

- Show per-chunk claim polygons grouped by faction.
- Display faction name at claim center.
- Configurable label scale, visibility zoom level, and minimap toggle.

## Dependencies

- JourneyMap (client API)
- Recruits mod
- (Optional) Cloth Config + AutoConfig for in-game config GUI

Adjust JDK / Forge version according to the Minecraft version you target.

## Installation

1. Build the mod (see _Building from source_).
2. Copy the resulting `.jar` into your Minecraft `mods/` folder.
3. Launch the game with the matching Forge profile and required mods.

## Configuration

- If the build uses **Cloth Config + AutoConfig**, open the Mod Options / Config screen (or Mod Menu) to edit:
  - `nameAppearMaxZoom` — maximum map zoom at which faction labels appear.
  - `fontScale` — multiplier for faction label size.
  - `showOnMinimap` — toggle showing labels on minimap (may not be supported depending on JourneyMap version).

- If using the plain **ForgeConfigSpec** variant, edit the generated TOML files in the `config/` folder.

## Building from source

From the project root (Gradle wrapper included):

```bash
# Run the client (dev environment)
./gradlew runClient

# Build the release jar
./gradlew build
# Built jar will be in build/libs/
```
