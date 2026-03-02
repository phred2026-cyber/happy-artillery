# 🔥 Happy Artillery

A Fabric mod for Minecraft 1.21.x that turns **Happy Ghasts** into rideable artillery — with enchanted fireballs, a haunting cry, a heat system, and full config support.

**By [PyreHaven](https://pyrehaven.xyz)**

> 💬 **Questions?** Join the [PyreHaven Discord](https://discord.gg/tZ6Hx2ETA3)
> 🌐 **More about PyreHaven:** [pyrehaven.xyz](https://pyrehaven.xyz)

---

## How It Works

Mount a Happy Ghast and look at your hotbar:

- **Slot 5 (Fire Charge)** — The item in slot 5 gets temporarily "fire enchanted", glowing to signal it is ready. Right-click to launch a fireball.
- **Slot 6 (Ghast Tear)** — Right-click to unleash a terrifying Ghast Cry (scream ability).

### The Enchantment System

The fire enchantment on your slot 5 item is **temporary** — it appears when you mount and signals the ability is charged. This is not a real item modification; it's purely visual/gameplay.

**You do NOT need to gather or craft a Ghast Tear or Fire Charge.** When there's no item primed in the slot, a temporary Fire Charge or Ghast Tear appears automatically so you can always fire and cry. The enchant fades when not in use.

---

## Features

- 🔥 **Enchanted fireball launcher** — slot 5 glows when primed and ready to fire
- 😱 **Ghast Cry** — slot 6 scream ability with cooldown
- 📦 **Ammo system** — up to 200 fireballs, regenerating passively over time
- 🌡️ **Heat & Overheat** — rapid firing builds heat; overheat = big sphere explosion
- 🗺️ **Biome mechanics** — heat and cooling rates change in Nether, hot, and cold biomes
- ⚙️ **Fully configurable** — every value tunable via `config/happy-artillery.json`

---

## Requirements

- Minecraft **1.21.x** (tested on 1.21.1)
- [Fabric Loader](https://fabricmc.net/) >= 0.16.14
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21+

**Works in singleplayer and multiplayer.** For servers, install server-side only — clients do NOT need it. Every player who joins can use it automatically.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Drop [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods/` folder
3. Drop `happy-artillery-X.X.X.jar` into your `mods/` folder
4. Launch — a default config is created at `config/happy-artillery.json`

---

## Heat Mechanics

Each shot builds heat. Hit the overheat limit and your ghast detonates in a fireball sphere.

| Biome Type | Heat/Shot | Cooling |
|---|---|---|
| Normal (BASE) | 1.0 | -1 every 3.0s |
| Hot (temp ≥ 1.5) | 2.0 | -1 every 6.0s |
| Cold (temp ≤ 0.0 / End) | 0.5 | -1 every 1.5s |
| Nether | 3.0 | ❌ No cooling |

Submerging in **water** rapidly cools the ghast but prevents firing.

---

## Configuration

Config: `config/happy-artillery.json` (auto-created on first launch)

```json
{
  "fireballAmmoMax": 200,
  "fireballAmmoCost": 1,
  "ammoDeliveryIntervalMin": 5,
  "shootCooldownSeconds": 0.25,
  "cryCooldownSeconds": 10.0,
  "baseOverheatLimit": 60,
  "baseHeatPerShot": 1.0,
  "baseCoolIntervalSeconds": 3.0,
  "hotBiomeOverheatLimit": 60,
  "hotBiomeHeatPerShot": 2.0,
  "hotBiomeCoolIntervalSeconds": 6.0,
  "coldBiomeOverheatLimit": 60,
  "coldBiomeHeatPerShot": 0.5,
  "coldBiomeCoolIntervalSeconds": 1.5,
  "netherOverheatLimit": 60,
  "netherHeatPerShot": 3.0,
  "netherNoCooldown": true
}
```

Changes take effect on server/game restart.

---

## Building From Source

```bash
git clone https://github.com/phred2026-cyber/happy-artillery
cd happy-artillery
./gradlew build
# Output: build/libs/happy-artillery-1.0.0.jar
```

---

## License

MIT — see [LICENSE](LICENSE)

---

## Credits

- **[PyreHaven](https://pyrehaven.xyz)** — organization
- **OG Moo-cow** — original author
