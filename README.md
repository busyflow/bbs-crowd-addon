# BBS Crowd Addon

Standalone crowd animation, procedural spawning, formation modeling, and behavioral AI add-on for **BBS Flow** (Minecraft 1.20.1 Fabric).

## Features

- **Crowd Spawning & Formations**: Circle, Grid, Line, Cluster, and custom painted areas.
- **Crowd Behaviors**: Idle, Wander, Follow, Charge, Flee, Look at target, and custom keyframed actions.
- **Crowd Keyframe Channels**: Dedicated channels for visibility, behavior, look target, jump, motion paths, texture variations, and tint colors.
- **In-World Area Brush**: Paint and erase crowd placement boundaries directly inside the film editor viewport.
- **Crowd Form & Renderers**: Seamless rendering of multi-entity crowds with custom armor, models, and skin desynchronization.
- **Automated ASM Mixin Auditor**: Includes `gradlew auditAllMixins` for static bytecode verification against BBS base jars with zero runtime mixin injection crashes.
- **Dynamic Modrinth Database Sync**: Fast synchronization with local Modrinth instance state.

## Building

```bash
./gradlew build
```

To run the bytecode mixin auditor:

```bash
./gradlew auditAllMixins
```

## License

MIT
