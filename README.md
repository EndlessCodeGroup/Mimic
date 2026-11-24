[![Version](https://img.shields.io/maven-central/v/ru.endlesscode.mimic/mimic-api?style=flat-square)][mavenCentral]
[![Build Status](https://img.shields.io/github/actions/workflow/status/EndlessCodeGroup/Mimic/main.yml?branch=develop&style=flat-square)][ci]
[![Code Quality](https://img.shields.io/codacy/grade/2509a9f8d248484d9cfd6376eb9426a1?style=flat-square)][codacy]
[![license](https://img.shields.io/github/license/EndlessCodeGroup/Mimic.svg?style=flat-square)][license]
[![Discord](https://img.shields.io/discord/314362656909819904?color=7389da&logo=discord&style=flat-square)](https://discord.gg/5NfPsgb)

<!-- Start of the Modrinth description -->

![Mimic](https://raw.githubusercontent.com/EndlessCodeGroup/Mimic/develop/images/logo.png)

> **Links:**
> [SpigotMC](https://www.spigotmc.org/resources/82515/),
> [Hangar](https://hangar.papermc.io/EndlessCode/Mimic)

**One API for every RPG plugin.**  
Think of it as [Vault](https://www.spigotmc.org/resources/34315/) for RPG plugins: Vault connects plugins to any economy or permissions plugin, Mimic does the same for levels, classes, resources like mana, items and blocks.

You only need Mimic if another plugin requires it.

## Installation

1. Make sure you run **Paper 1.20.6+** (or a fork) on **Java 21**.
2. Put Mimic into the `plugins/` folder.
3. Restart the server.

That's it. Mimic detects installed RPG plugins automatically.

## Supported plugins

| API            | Plugins                                                                                  |
|----------------|------------------------------------------------------------------------------------------|
| **Levels**     | Vanilla experience, AuraSkills, mcMMO, ValhallaMMO, Jobs Reborn, MMOCore, Heroes, Fabled |
| **Classes**    | MMOCore, Heroes, Fabled                                                                  |
| **Resources**  | Vanilla health, food and air, MMOCore, Heroes, AuraSkills, Fabled                        |
| **Items**      | Vanilla items, CraftEngine, Oraxen, Nexo, MMOItems, CustomItems, EcoItems, MythicMobs, ExecutableItems, ItemsAdder |
| **Blocks**     | Vanilla blocks, CraftEngine, ItemsAdder, Oraxen, Nexo                                    |

Is your favorite plugin missing? [Open an issue](https://github.com/EndlessCodeGroup/Mimic/issues) or ask its author to support Mimic.

## Configuration

If several plugins provide the same feature, choose which one to use with `/mimic config`.
Every option in its output is clickable.

## Commands

All commands require the `mimic.admin` permission.

| Command            | Description                          |
|--------------------|--------------------------------------|
| `/mimic`           | Show Mimic status                    |
| `/mimic config`    | Show and change Mimic config         |
| `/mimic levels`    | Manage player's levels               |
| `/mimic classes`   | Manage player's classes              |
| `/mimic resources` | Manage player's mana and similar     |
| `/mimic inventory` | Player's inventory info              |
| `/mimic items`     | Give, find and compare items         |
| `/mimic blocks`    | Set, break, find and compare blocks  |

Commands without arguments show info and list available subcommands: click a subcommand to use it, hover to see usage.

## Links

- [Item and block ID formats](https://github.com/EndlessCodeGroup/Mimic/tree/develop/mimic-bukkit#supported-implementations) — for configuring plugins that use Mimic IDs
- [Developer guide](https://github.com/EndlessCodeGroup/Mimic/tree/develop/mimic-bukkit-api)
- [Source code](https://github.com/EndlessCodeGroup/Mimic)

<!-- End of the Modrinth description -->

## For developers

If you are developer you can use [Gradle] or [Maven] to plug Mimic to your plugin.

### Gradle

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ru.endlesscode.mimic:mimic-bukkit-api:0.10.0")
}
```

### Maven
```xml
<dependency>
  <groupId>ru.endlesscode.mimic</groupId>
  <artifactId>mimic-bukkit-api</artifactId>
  <version>0.10.0</version>
  <scope>provided</scope>
</dependency>
```

### Available APIs

- [Levels] - API to work with player's levels: classes, skills, professions or jobs a player levels up in.
  Makes it possible to use levels from several plugins at once.
- [LevelSystem] *(deprecated, use [Levels])* - API to work with player's single level and experience points.
- [Classes] - API to work with player's classes: classes and races held in class slots, with subclasses and class levels.
  Makes it possible to use classes from several plugins at once.
- [ClassSystem] *(deprecated, use [Classes])* - API to work with player's classes.
- [Resources] - API to work with player's resources: mana, stamina and similar. Read them, take costs, give them back.
- [PlayerInventory] *(experimental)* - API to access player's inventories content.
- [Items] - API to create and identify vanilla items and custom items created with plugins.
- [ItemsRegistry] *(deprecated, use [Items])* - API to create and validate custom items created with plugins.
- [Blocks] - API to identify, set and break vanilla blocks and custom blocks created with plugins.

### Guides

- [mimic-bukkit-api](mimic-bukkit-api) - mimic-api adapted for Bukkit. Use it in Paper plugins.
  Contains the developer guide with API usage examples.
- [mimic-bukkit](mimic-bukkit) - Bukkit plugin containing some APIs implementations.
  It also provides commands to test your API implementation.
  Use it as example of Mimic APIs usage and implementation.
  Item and block ID formats of the supported plugins are described there.

## Contributing
Pull requests are welcome.
For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## AI usage

Mimic was started in 2017 and was written by hand for most of its history.
Since late 2026 it is developed with the help of AI. It is the only way I can keep this project going in the time I have.

**Public API is designed by me.** AI doesn't decide what the API looks like.

**AI helps with:**

- Research: how RPG and item plugins expose their data and how other plugins consume it.
- Implementation behind the API, and integrations with other plugins.
- Review, refactoring, tests, and splitting changes into reviewable commits.

I review the code before it is merged.

[codacy]: https://app.codacy.com/gh/EndlessCodeGroup/Mimic/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade
[mavenCentral]: https://central.sonatype.com/namespace/ru.endlesscode.mimic
[ci]: https://github.com/EndlessCodeGroup/Mimic/actions/
[license]: LICENSE

[Gradle]: https://gradle.org/
[Maven]: https://maven.apache.org/

[LevelSystem]: mimic-api/src/main/kotlin/level/LevelSystem.kt
[Levels]: mimic-bukkit-api/src/main/kotlin/level/Levels.kt
[Classes]: mimic-bukkit-api/src/main/kotlin/classes/Classes.kt
[ClassSystem]: mimic-api/src/main/kotlin/classes/ClassSystem.kt
[Resources]: mimic-bukkit-api/src/main/kotlin/resources/Resources.kt
[PlayerInventory]: mimic-api/src/main/kotlin/inventory/PlayerInventory.kt
[Items]: mimic-bukkit-api/src/main/kotlin/items/Items.kt
[ItemsRegistry]: mimic-api/src/main/kotlin/items/ItemsRegistry.kt
[Blocks]: mimic-bukkit-api/src/main/kotlin/blocks/Blocks.kt
