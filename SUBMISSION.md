# Add Local Live Bridge

Local Live Bridge exports structured, read-only game observations to a local
JSON file for use by external assistants.

## Intended use

Local Live Bridge helps external assistants give players explanations grounded in what they are seeing in RuneScape. It connects general game knowledge with the dialogue, interface elements, and inventory items relevant to a player’s question.

A new player could ask, “What does this instructor mean?” or “What is this item used for?” and receive an explanation informed by the game context. This supports learning through conversation and helps players understand the reasons behind tutorial instructions.

The plugin exports structured, read-only observations to a local file. It does not generate mouse or keyboard input, execute game actions, or accept gameplay commands.

## Implementation

The plugin captures observations at a 250-millisecond interval (approximately
four snapshots per second): visible interface
text/actions/bounds, inventory, visible NPC information, local-player location
and animation state, and camera pitch/yaw. Snapshots use RuneLite's Filepath API
and a background file writer. There is no HTTP server, network upload, input
injection, game action implementation, or command endpoint.

Game observations are collected only while logged in. Login-screen widgets,
credentials, other-player entities, and chatbox history are excluded. Consumers
must reject stale snapshots.

## Validation and review status

The Java 11 Gradle build and Plugin Hub build checks passed for the earlier
source version. Live logged-in capture remains unverified. The default-enabled
setting has been corrected in response to automated review; this revision
requires fresh build checks and in-game validation before release.
