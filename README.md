# 🔥 Happy Artillery

A Fabric mod for Minecraft 1.21.x that turns Happy Ghasts into rideable artillery. Mount up, aim, and start lobbing fireballs.

**By OG Moo-cow / Pyrehaven**

---

## What It Does

- **Shoot fireballs** from a Happy Ghast by right-clicking with a **Fire Charge** in your hand while riding
- **Ghast Cry** ability — right-click with a **Ghast Tear** to let out a terrifying scream (great for trolling)
- **Ammo system** — Ghasts hold up to 200 fireballs, regenerating passively over time
- **Heat & Overheat** — rapid firing builds heat; overheat = big explosion in all directions
- **Biome-based mechanics** — heat behaves differently in the Nether vs cold biomes vs normal biomes
- **Fully configurable** — every number is tunable via a JSON config file

---

## Requirements

- Minecraft **1.21.x** (tested on 1.21.11)
- [Fabric Loader](https://fabricmc.net/) >= 0.16.14
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21+

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and drop it in your `mods/` folder
3. Drop `happy-artillery-X.X.X.jar` into your `mods/` folder
4. Launch the game — a default config will be created at `config/happy-artillery.json`

> Works on **servers** and **clients**. For multiplayer, install on the server. Clients don't need it.

---

## Usage

1. Find and mount a **Happy Ghast** (added in 1.21.5)
2. Hold a **Fire Charge** and right-click to shoot a fireball
3. Hold a **Ghast Tear** and right-click to unleash a scream
4. Watch your heat gauge — rapid firing builds heat, and overheating detonates the ghast

---

## Heat Mechanics

Firing builds heat. When heat hits the limit, the ghast explodes in a sphere of fireballs.

Heat behavior depends on your biome:

| Biome Type | Overheat Limit | Heat/Shot | Cooling Interval |
|---|---|---|---|
| Normal | 60 | 0.5 | 1.5s per -1 heat |
| Hot (temp ≥ 1.5) | 60 | 0.5 | 1.5s per -1 heat |
| Cold (temp ≤ 0.0 / End) | 60 | 0.5 | 1.5s per -1 heat |
| Nether | 60 | 0.5 | ❌ No cooling |

Being **submerged in water** rapidly cools the ghast but prevents firing.

---

## Configuration

Config file: `config/happy-artillery.json` (auto-created on first launch)

```json
{
  "fireballAmmoMax": 200,
  "fireballAmmoCost": 1,
  "ammoDeliveryIntervalMin": 5,
  "shootCooldownSeconds": 0.25,
  "fireRestartDelaySeconds": 0.5,
  "cryCooldownSeconds": 10.0,
  "baseOverheatLimit": 60,
  "baseHeatPerShot": 0.5,
  "baseCoolIntervalSeconds": 1.5,
  "hotBiomeOverheatLimit": 60,
  "hotBiomeHeatPerShot": 0.5,
  "hotBiomeCoolIntervalSeconds": 1.5,
  "coldBiomeOverheatLimit": 60,
  "coldBiomeHeatPerShot": 0.5,
  "coldBiomeCoolIntervalSeconds": 1.5,
  "netherOverheatLimit": 60,
  "netherHeatPerShot": 0.5,
  "netherNoCooldown": true,
  "waterCooldownRate": 8,
  "waterCooldownLimit": 5,
  "fireballExplosionPower": 2,
  "overheatExplosionPower": 4.0,
  "overheatExplosionCreatesFire": true,
  "cryVolume": 3.0
}
```

| Key | Default | Description |
|---|---|---|
| `fireballAmmoMax` | 200 | Max ammo a ghast can hold |
| `fireballAmmoCost` | 1 | Ammo used per shot |
| `ammoDeliveryIntervalMin` | 5 | Minutes between passive ammo ticks |
| `shootCooldownSeconds` | 0.25 | Minimum seconds between shots |
| `fireRestartDelaySeconds` | 0.5 | Seconds after a shot before heat cools |
| `cryCooldownSeconds` | 10.0 | Cooldown on the cry ability |
| `baseOverheatLimit` | 60 | Heat cap in normal biomes |
| `baseHeatPerShot` | 0.5 | Heat per shot in normal biomes |
| `baseCoolIntervalSeconds` | 1.5 | Seconds between each -1 heat in normal biomes |
| `hotBiomeOverheatLimit` | 60 | Heat cap in hot biomes |
| `hotBiomeHeatPerShot` | 0.5 | Heat per shot in hot biomes |
| `hotBiomeCoolIntervalSeconds` | 1.5 | Cooling speed in hot biomes |
| `coldBiomeOverheatLimit` | 60 | Heat cap in cold biomes / End |
| `coldBiomeHeatPerShot` | 0.5 | Heat per shot in cold biomes |
| `coldBiomeCoolIntervalSeconds` | 1.5 | Cooling speed in cold biomes |
| `netherOverheatLimit` | 60 | Heat cap in the Nether |
| `netherHeatPerShot` | 0.5 | Heat per shot in the Nether |
| `netherNoCooldown` | true | Disables cooling in the Nether |
| `waterCooldownRate` | 8 | Heat removed per second while submerged |
| `waterCooldownLimit` | 5 | Minimum heat water cooling can reach |
| `fireballExplosionPower` | 2 | Power of each fireball (vanilla ghast = 1) |
| `overheatExplosionPower` | 4.0 | Power of the overheat explosion |
| `overheatExplosionCreatesFire` | true | Whether overheat explosion spawns fire |
| `cryVolume` | 3.0 | Volume multiplier for the cry scream |

Changes take effect on **server/game restart**.

---

## Building From Source

```bash
git clone https://github.com/Phred2026-cyber/happy-artillery
cd happy-artillery
./gradlew build
# Output: build/libs/happy-artillery-1.0.0.jar
```

---

## License

MIT — do whatever you want, just don't claim you made it.

---

## Credits

- **OG Moo-cow** — mod creator
- **Pyrehaven** — [pyrehaven.xyz](https://pyrehaven.xyz)
