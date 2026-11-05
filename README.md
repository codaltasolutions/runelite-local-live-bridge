# Local Live Bridge

An experimental RuneLite plugin that exports live observations to a local JSON
file for an external desktop assistant. This is a prototype seeking review,
not a Plugin Hub approved release.

## Functionality and intended use

When explicitly enabled, the plugin captures at most one snapshot per second:

- Logged-in widget text, actions, and canvas bounds, including tutorial dialogue.
- Inventory item identifiers, names, quantities, and slots.
- Visible NPC identifiers, names, locations, available actions, and canvas bounds.
- The local player's location and animation state, and camera pitch/yaw.

It writes `state.json` inside its RuneLite-managed plugin data directory. It uses
RuneLite's `Filepath` API, and performs disk writes on a background executor.
Readers must reject snapshots whose `capturedAt` is more than three seconds old.
Canvas bounds are not desktop-window coordinates.

The requested use case is an external AI assistant observing Tutorial Island,
potentially paired with separate desktop mouse/keyboard automation. The plugin
itself does not issue inputs, invoke game actions, inject code, make network
requests, or accept commands. This intended integration is disclosed for
maintainer review; this project makes no claim that the use case complies with
RuneLite or Jagex rules or will be accepted into the Plugin Hub.

## Data handling

The plugin is disabled by default. It does not inspect login widgets or collect
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

The original prototype produced class files and passed a launcher smoke check,
but never produced a verified logged-in snapshot. This revised version adds
standard build configuration and CI. In-game behavior is unverified.

Manual checks required before release: enable/disable the plugin; confirm fresh
timestamps; inspect tutorial dialogue and inventory changes; confirm logout
replaces game data with status; confirm chat history and other-player entities
are absent; verify consumers stop on stale data; check CPU and disk overhead.

## License

BSD-2-Clause. See LICENSE.
