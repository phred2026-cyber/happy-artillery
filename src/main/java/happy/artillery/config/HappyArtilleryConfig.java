package happy.artillery.config;

/**
 * Configurable values for Happy Artillery.
 * Loaded from config/happy-artillery.json in the game directory.
 * All defaults preserve original vanilla behavior.
 */
public class HappyArtilleryConfig {

    // ── Ammo ────────────────────────────────────────────────────────────────
    /** Maximum ammo a Happy Ghast can hold. */
    public int fireballAmmoMax = 200;
    /** Ammo consumed per fireball. */
    public int fireballAmmoCost = 1;
    /** Minutes between passive ammo deliveries (when below max). */
    public int ammoDeliveryIntervalMin = 5;

    // ── Shoot / Cooldown ────────────────────────────────────────────────────
    /** Minimum seconds between shots. */
    public double shootCooldownSeconds = 0.25;
    /** Seconds after a shot before cooldown starts counting. */
    public double fireRestartDelaySeconds = 0.5;
    /** Cooldown in seconds for the Cry ability. */
    public double cryCooldownSeconds = 10.0;

    // ── Heat — Base / Normal biomes ─────────────────────────────────────────
    /** Overheat threshold for normal biomes. */
    public int baseOverheatLimit = 60;
    /** Heat added per shot in normal biomes. */
    public double baseHeatPerShot = 0.5;
    /** Seconds between each -1 heat tick in normal biomes. */
    public double baseCoolIntervalSeconds = 1.5;

    // ── Heat — Hot biomes (temp >= 1.5) ────────────────────────────────────
    public int hotBiomeOverheatLimit = 60;
    public double hotBiomeHeatPerShot = 0.5;
    public double hotBiomeCoolIntervalSeconds = 1.5;

    // ── Heat — Cold biomes (temp <= 0.0 / End) ─────────────────────────────
    public int coldBiomeOverheatLimit = 60;
    public double coldBiomeHeatPerShot = 0.5;
    public double coldBiomeCoolIntervalSeconds = 1.5;

    // ── Heat — Nether ───────────────────────────────────────────────────────
    public int netherOverheatLimit = 60;
    public double netherHeatPerShot = 0.5;
    /** When true, heat never cools down in the Nether. */
    public boolean netherNoCooldown = true;

    // ── Water cooling ───────────────────────────────────────────────────────
    /** Heat removed per second while submerged. */
    public int waterCooldownRate = 8;
    /** Heat floor when water-cooled (water can't cool below this). */
    public int waterCooldownLimit = 5;

    // ── Explosions ──────────────────────────────────────────────────────────
    /** Power of each fireball explosion (vanilla ghast = 1). */
    public int fireballExplosionPower = 2;
    /** Power of the overheat explosion. */
    public float overheatExplosionPower = 4.0f;
    /** Whether the overheat explosion spawns fire. */
    public boolean overheatExplosionCreatesFire = true;

    // ── Sounds ──────────────────────────────────────────────────────────────
    /** Volume multiplier for the Cry scream ability. */
    public float cryVolume = 3.0f;
}
