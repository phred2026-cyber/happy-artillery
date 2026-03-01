package happy.artillery.util;

import happy.artillery.config.HAConstants;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ammo, heat, and cooldown state for ghasts and players.
 * All state is memory-resident (resets on server restart).
 */
public class CooldownTracker {
    public static enum BiomeType {
        // overheatLimit=60 for all
        // Cold: 0.5 heat/shot, cools 1 per 1.5s
        // Base: 1.0 heat/shot, cools 1 per 3s
        // Hot: 2.0 heat/shot, cools 1 per 6s (slower)
        // Nether: 3.0 heat/shot, no cooling
        COLD(60, 0.5, 1.5),
        BASE(60, 1.0, 3.0),
        HOT(60, 2.0, 6.0),
        NETHER(60, 3.0, Double.POSITIVE_INFINITY), // Positive infinity means no cooling
        // End dimension: very cool - low heat per shot and fast cooling (treated like cold)
        END(60, 0.5, 1.5);

        public final int overheatLimit;
        public final double heatPerShot;
        public final double coolIntervalSeconds;

        BiomeType(int overheatLimit, double heatPerShot, double coolIntervalSeconds) {
            this.overheatLimit = overheatLimit;
            this.heatPerShot = heatPerShot;
            this.coolIntervalSeconds = coolIntervalSeconds;
        }
    }

/**
 * Manages ammo, heat, and cooldown state for ghasts and players.
 * All state is memory-resident (resets on server restart).
 */
    private static final ConcurrentHashMap<UUID, Long> lastShootTime = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> timeSinceLastShot = new ConcurrentHashMap<>(); // Counts down from 0.5s after each shot
    private static final ConcurrentHashMap<UUID, Long> coolingStartTime = new ConcurrentHashMap<>(); // When cooling started (to count down)
    private static final ConcurrentHashMap<UUID, Double> fireballHeat = new ConcurrentHashMap<>(); // Use Double to track decimal heat values
    private static final ConcurrentHashMap<UUID, Long> lastHeatUpdate = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, BiomeType> ghastBiomeType = new ConcurrentHashMap<>(); // Track biome per ghast
    private static final ConcurrentHashMap<UUID, Integer> ghastAmmo = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> lastAmmoDelivery = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> lastCryTime = new ConcurrentHashMap<>();

    public static boolean canShoot(UUID ghastId) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) (HAConstants.SHOOT_COOLDOWN_SECONDS() * 1000);
        long last = lastShootTime.getOrDefault(ghastId, 0L);
        return (now - last) >= cooldownMs;
    }

    public static float getRemainingShootCooldown(UUID ghastId) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) (HAConstants.SHOOT_COOLDOWN_SECONDS() * 1000);
        long last = lastShootTime.getOrDefault(ghastId, 0L);
        long remaining = cooldownMs - (now - last);
        return remaining > 0 ? (remaining / 1000f) : 0f;
    }

    public static void recordShot(UUID ghastId) {
        long now = System.currentTimeMillis();
        lastShootTime.put(ghastId, now);
        timeSinceLastShot.put(ghastId, now); // Set to current time, will countdown toward 0
        coolingStartTime.remove(ghastId); // Reset cooling timer when firing
        lastAmmoDelivery.put(ghastId, now);
    }

    public static float getTimeSinceLastShot(UUID ghastId) {
        long now = System.currentTimeMillis();
        long lastShot = timeSinceLastShot.getOrDefault(ghastId, 0L);
        long elapsedMs = now - lastShot;
        long remainingMs = (long) (HAConstants.FIRE_RESTART_DELAY_SECONDS() * 1000) - elapsedMs;
        return remainingMs > 0 ? (remainingMs / 1000f) : 0f;
    }

    public static int getCoolingSecondsRemaining(UUID ghastId) {
        long now = System.currentTimeMillis();
        BiomeType biome = ghastBiomeType.getOrDefault(ghastId, BiomeType.BASE);
        
        // Nether has no cooldown
        if (biome == BiomeType.NETHER) {
            return -1; // Special flag for "no cooling"
        }
        
        // If not currently cooling (still in firing mode), reset cooling timer
        float timeSinceShot = getTimeSinceLastShot(ghastId);
        if (timeSinceShot > 0) {
            coolingStartTime.remove(ghastId);
            return 0;
        }
        
        // If cooling just started, initialize the timer and calculate total cooling time
        if (!coolingStartTime.containsKey(ghastId)) {
            double heat = getFireballHeat(ghastId);
            if (heat > 0) {
                coolingStartTime.put(ghastId, now);
                // Total cooling time = heat / (1 / coolIntervalSeconds)
                // e.g., if heat=10 and coolInterval=1.5s, total = 10 * 1.5 = 15 seconds
                int totalCoolingSeconds = (int) (heat * biome.coolIntervalSeconds);
                return totalCoolingSeconds;
            }
            return 0;
        }
        
        // Calculate remaining cooling time
        long coolingStart = coolingStartTime.get(ghastId);
        long elapsedMs = now - coolingStart;
        double heat = getFireballHeat(ghastId);
        int totalCoolingSeconds = (int) (heat * biome.coolIntervalSeconds);
        int secondsElapsed = (int) (elapsedMs / 1000);
        
        return Math.max(0, totalCoolingSeconds - secondsElapsed);
    }

    public static boolean useAmmo(UUID ghastId) {
        return useAmmo(ghastId, HAConstants.FIREBALL_AMMO_COST());
    }

    public static boolean useAmmo(UUID ghastId, int amount) {
        updateGhastAmmo(ghastId);
        int currentAmmo = ghastAmmo.getOrDefault(ghastId, HAConstants.FIREBALL_AMMO_MAX());

        if (currentAmmo >= amount) {
            ghastAmmo.put(ghastId, currentAmmo - amount);
            return true;
        }
        return false;
    }

    public static int getAmmo(UUID ghastId) {
        updateGhastAmmo(ghastId);
        return ghastAmmo.getOrDefault(ghastId, HAConstants.FIREBALL_AMMO_MAX());
    }

    public static int getSecondsUntilNextAmmo(UUID ghastId) {
        updateGhastAmmo(ghastId);
        int currentAmmo = ghastAmmo.getOrDefault(ghastId, HAConstants.FIREBALL_AMMO_MAX());
        if (currentAmmo >= HAConstants.FIREBALL_AMMO_MAX()) return 0;

        long now = System.currentTimeMillis();
        long lastDelivery = lastAmmoDelivery.getOrDefault(ghastId, now);
        long deliveryIntervalMs = (long) HAConstants.AMMO_DELIVERY_INTERVAL_MIN() * 60_000L;
        long timeUntilNext = deliveryIntervalMs - (now - lastDelivery);

        return timeUntilNext > 0 ? (int) (timeUntilNext / 1000) + 1 : 0;
    }

    private static void updateGhastAmmo(UUID ghastId) {
        long now = System.currentTimeMillis();
        long lastDelivery = lastAmmoDelivery.getOrDefault(ghastId, now);
        int currentAmmo = ghastAmmo.getOrDefault(ghastId, HAConstants.FIREBALL_AMMO_MAX());

        if (currentAmmo < HAConstants.FIREBALL_AMMO_MAX()) {
            long timePassed = now - lastDelivery;
            long deliveryIntervalMs = (long) HAConstants.AMMO_DELIVERY_INTERVAL_MIN() * 60_000L;
            long deliveriesToAdd = timePassed / deliveryIntervalMs;

            if (deliveriesToAdd > 0) {
                int newAmmo = Math.min(currentAmmo + (int) deliveriesToAdd, HAConstants.FIREBALL_AMMO_MAX());
                ghastAmmo.put(ghastId, newAmmo);
                lastAmmoDelivery.put(ghastId, lastDelivery + (deliveriesToAdd * deliveryIntervalMs));
            }
        } else {
            lastAmmoDelivery.put(ghastId, now);
        }
    }

    public static boolean canCry(UUID playerId) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) (HAConstants.CRY_COOLDOWN_SECONDS() * 1000);
        long last = lastCryTime.getOrDefault(playerId, 0L);
        return (now - last) >= cooldownMs;
    }

    public static int getRemainingCryCooldown(UUID playerId) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) (HAConstants.CRY_COOLDOWN_SECONDS() * 1000);
        long last = lastCryTime.getOrDefault(playerId, 0L);
        long remaining = cooldownMs - (now - last);
        return remaining > 0 ? (int) (remaining / 1000) + 1 : 0;
    }

    public static void recordCry(UUID playerId) {
        lastCryTime.put(playerId, System.currentTimeMillis());
    }

    public static boolean addFireballHeat(UUID ghastId) {
        updateHeat(ghastId);
        BiomeType biome = ghastBiomeType.getOrDefault(ghastId, BiomeType.BASE);
        double currentHeat = fireballHeat.getOrDefault(ghastId, 0.0);
        double newHeat = currentHeat + biome.heatPerShot;
        fireballHeat.put(ghastId, newHeat);
        // Reset cooling timer when a new shot is fired
        coolingStartTime.remove(ghastId);
        return newHeat >= biome.overheatLimit;
    }

    public static double getFireballHeat(UUID ghastId) {
        updateHeat(ghastId);
        double heat = fireballHeat.getOrDefault(ghastId, 0.0);
        return Math.max(0.0, heat);
    }

    public static int getFireballOverheatRemaining(UUID ghastId) {
        BiomeType biome = ghastBiomeType.getOrDefault(ghastId, BiomeType.BASE);
        double currentHeat = getFireballHeat(ghastId);
        return Math.max(0, (int) (biome.overheatLimit - currentHeat));
    }

    public static void setFireballHeat(UUID ghastId, double heat) {
        fireballHeat.put(ghastId, heat);
        lastHeatUpdate.put(ghastId, System.currentTimeMillis());
        // Reset cooling timer when heat is manually set
        coolingStartTime.remove(ghastId);
    }
    
    /**
     * Update the biome type for a ghast and recalculate cooling timer
     */
    public static void setBiomeType(UUID ghastId, BiomeType biome) {
        ghastBiomeType.put(ghastId, biome);
        // Reset cooling timer so it recalculates with new biome's cool interval
        coolingStartTime.remove(ghastId);
    }
    
    public static BiomeType getBiomeType(UUID ghastId) {
        return ghastBiomeType.getOrDefault(ghastId, BiomeType.BASE);
    }

    public static void applyWaterCooling(UUID ghastId) {
        long now = System.currentTimeMillis();
        long lastUpdate = lastHeatUpdate.getOrDefault(ghastId, now);
        long timePassed = (now - lastUpdate) / 1000;

        if (timePassed >= 1) {
            double currentHeat = fireballHeat.getOrDefault(ghastId, 0.0);
            int coolingAmount = (int) (timePassed * HAConstants.WATER_COOLDOWN_RATE());
            double newHeat = Math.max((double) HAConstants.WATER_COOLDOWN_LIMIT(), currentHeat - coolingAmount);

            fireballHeat.put(ghastId, newHeat);
            lastHeatUpdate.put(ghastId, now);
        }
    }

    private static void updateHeat(UUID ghastId) {
        BiomeType biome = ghastBiomeType.getOrDefault(ghastId, BiomeType.BASE);
        
        // Nether has no cooldown
        if (biome == BiomeType.NETHER) {
            return;
        }
        
        // Only cool down when NOT firing
        float timeSinceShot = getTimeSinceLastShot(ghastId);
        if (timeSinceShot > 0) {
            // Still in firing mode, don't cool
            return;
        }
        
        long now = System.currentTimeMillis();
        long lastUpdate = lastHeatUpdate.getOrDefault(ghastId, now);
        double timePassed = (now - lastUpdate) / 1000.0;

        // Only cool down if enough time has passed based on biome's cool interval
        // Each interval reduces heat by 1
        if (timePassed >= biome.coolIntervalSeconds) {
            double currentHeat = fireballHeat.getOrDefault(ghastId, 0.0);
            // Calculate how many full intervals have passed and reduce by that amount
            long fullIntervals = (long) (timePassed / biome.coolIntervalSeconds);
            double newHeat = Math.max(0.0, currentHeat - fullIntervals);

            fireballHeat.put(ghastId, newHeat);
            lastHeatUpdate.put(ghastId, now);
        } else if (!lastHeatUpdate.containsKey(ghastId)) {
            lastHeatUpdate.put(ghastId, now);
        }
    }

    /**
     * Passive global tick to allow ammo regeneration even when no player interacts.
     * Call once per server tick.
     */
    public static void passiveGlobalTick() {
        for (UUID id : ghastAmmo.keySet()) {
            updateGhastAmmo(id);
        }
    }
}
