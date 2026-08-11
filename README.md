# CHRONOCRYPT

A standalone offline Android roguelike deckbuilding game about weaponizing time travel.

## V1 feature set
- Three-act run with 12 floors per act and route choices: combat, elite, event, shop, anchor station, boss.
- 132 playable cards across Archive, Vanguard, Oracle, Entropy, Weaver, Null, and Signature families.
- Three combat eras: Past, Present, Future. Matching card era grants Synchrony; cross-era plays generate Paradox.
- Past Echoes repeat attacks on the next turn; Future Forecasts resolve after two turn starts.
- One causality Rewind per turn restores the start-of-turn combat snapshot while permanently increasing Paradox.
- 24 relics, 12 narrative causal events, deck upgrades/removal, persistent run saves, tutorial, codex, and Chronicle stats.
- Three bosses: The Chronophage, The Retrograde Oracle, The First / Last Machine.
- Procedural sci-fi UI, animated timeline visual language, haptics, and synthesized sound effects.
- Interrupted-node recovery prevents incomplete encounters from being silently skipped after Android process death.
- No network permission or runtime service dependency.

## Android
- Package: `com.chronocrypt.game`
- Min SDK: 26
- Target / Compile SDK: 35
- Portrait, touch-first UI tuned around modern tall Pixel displays.

## Build
The repository includes a GitHub Actions workflow that builds `app-debug.apk` with Gradle 8.9 / AGP 8.7.3 and Android SDK 35.
