# Marlow's Crystal Optimizer — Forge 1.16.5 port

This client-only Forge 1.16.5 module ports the core behavior of Marlow's
Crystal Optimizer: when a valid End Crystal attack is sent, it removes that
crystal from the local client immediately so it no longer blocks the next
placement while the server response is travelling back. It does not cancel,
alter, or accelerate the serverbound attack packet.

The project uses Java 8 and official Forge 1.16.5 mappings. Build with GitHub Actions
(see `.github/workflows/build-crystaloptimizer.yml`) or locally with
Gradle 6.9.x on Java 8:

```sh
gradle build
```

The jar lands in `build/libs/`.

Place the resulting jar in a Forge 1.16.5 client's `mods` folder. A server-side
ViaVersion/ViaBackwards setup may allow that 1.16.5 client to connect to a
1.21.11 server; the mod itself does not provide protocol translation.

This is a client-only End Crystal optimization. It does not speed up respawn
anchor interactions, and it cannot make server-side actions happen faster.

The upstream project is MIT-licensed. Its license is included in
`LICENSE-marlow-crystal-optimizer.txt`.
