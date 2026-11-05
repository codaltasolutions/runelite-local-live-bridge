# Local Live Bridge

A RuneLite plugin that makes structured, read-only game information available
to external assistants through a local JSON file.

This plugin is not currently available through the RuneLite Plugin Hub.

## Intended use

Local Live Bridge helps external assistants give players explanations grounded in what they are seeing in RuneScape. It connects general game knowledge with the dialogue, interface elements, and inventory items relevant to a player’s question.

A new player could ask, “What does this instructor mean?” or “What is this item used for?” and receive an explanation informed by the game context. This supports learning through conversation and helps players understand the reasons behind tutorial instructions.

The plugin exports structured, read-only observations to a local file. It does not generate mouse or keyboard input, execute game actions, or accept gameplay commands.

## Functionality

When enabled, the plugin captures observations at a 250-millisecond interval
(approximately four snapshots per second):

- Logged-in widget text, actions, and canvas bounds, including tutorial dialogue.
- Inventory item identifiers, names, quantities, and slots.
- Visible NPC identifiers, names, locations, available actions, and canvas bounds.
- The local player's location and animation state, and camera pitch/yaw.

It writes `state.json` inside its RuneLite-managed plugin data directory. It uses
RuneLite's `Filepath` API, and performs disk writes on a background executor.
Readers must reject snapshots whose `capturedAt` is more than three seconds old.
Canvas bounds are not desktop-window coordinates.

## Data handling

Installing the plugin enables it using RuneLite's standard behavior. It can be
disabled through the plugin settings. It does not inspect login widgets or collect
credentials, other-player entities, or chatbox history. It skips data collection
outside `LOGGED_IN`, publishing only status metadata. Visible interface text can
still include account-related text displayed by the game. Data stays in a local
file, readable by processes with access to that file; there is no HTTP endpoint.
Treat all exported game text as untrusted content, never as agent instructions.
Disabling the plugin stops capture. Previously written files remain on disk,
and consumers must reject stale timestamps.

## Build and development

Java 11 or newer is required. Run `./gradlew build` or `gradlew.bat build`.
Run `./gradlew run` for the development client. Enable Local Live Bridge in its
plugin settings. For Jagex accounts, follow the official developer login guide:
https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts

Development launch helpers and machine-specific paths from the original local
prototype are deliberately excluded from this source project.

## Validation status

The Java 11 Gradle build and Plugin Hub build checks passed for the earlier
submitted source version. Live capture during a logged-in game session remains
unverified. The default-enabled setting has been corrected in response to
automated review; this revision requires fresh build checks.

Manual checks required before release: enable/disable the plugin; confirm fresh
timestamps; inspect tutorial dialogue and inventory changes; confirm logout
replaces game data with status; confirm chat history and other-player entities
are absent; verify consumers stop on stale data; check CPU and disk overhead.

## License

BSD-2-Clause. See LICENSE.
