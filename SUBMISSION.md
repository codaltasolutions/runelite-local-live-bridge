# Proposed Plugin Hub submission

Title: Add Local Live Bridge (experimental local observation export)

This plugin exports visible interface text/actions/bounds, inventory, visible
NPC information, local-player position/animation, and camera state to a local
JSON file at most once per second. It is disabled by default, uses RuneLite's
Filepath API, and has no network endpoint or input/action implementation.

The intended consumer is an external desktop AI assistant for Tutorial Island,
potentially paired with separate mouse/keyboard automation. This is explicitly
disclosed for review, not presented as a conventional in-game helper. Acceptance
and rules compliance have not been established.

Validation: CI build is to be checked after publication. In-game behavior is
unverified; the earlier local prototype never produced a confirmed logged-in
snapshot. Keep the submission draft pending validation and maintainer guidance.

Generated-by: OpenAI Codex
