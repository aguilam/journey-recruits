# Journey Recruits

**Short notice:** This mod was "vibe-coded" for personal use and will most likely **not** be actively supported going forward. Use at your own risk.

## Overview

RecruitBridge integrates claims from the **Recruits** mod into **JourneyMap** overlays.

## Features

- Show per-chunk claim polygons grouped by faction.
- Display faction name at claim center.
- Configurable label visibility zoom level and minimap toggle.

## Dependencies

- Minecraft 1.21.1
- NeoForge 21.1.x
- JourneyMap
- Recruits mod
- (Optional) Cloth Config for the in-game config GUI

## Installation

1. Build the mod (see _Building from source_).
2. Copy the resulting `.jar` into your Minecraft `mods/` folder.
3. Launch the game with the matching NeoForge profile and the required mods.

## Configuration

Open the Mod Options / Config screen (Cloth Config) or edit `config/journeyrecruits-client.toml`:

- `nameShowMaxZoom` — maximum map zoom at which faction labels appear.
- `labelFontScaleFullscreen` — faction label size on the fullscreen map (default `2.5`).
- `labelFontScaleMinimap` — faction label size on the minimap (default `1.5`).
- `showLabelOnMinimap` — toggle showing labels on the minimap.

## Building from source

From the project root (Gradle wrapper included):

```bash
# Build the release jar
./gradlew build
# Built jar will be in build/libs/
```