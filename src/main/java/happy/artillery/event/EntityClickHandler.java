package happy.artillery.event;

import happy.artillery.config.HAConstants;
import happy.artillery.CustomDataComponents;
import happy.artillery.mixin.accessor.EntityWorldAccessor;
import happy.artillery.util.CooldownTracker;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles right-click interactions on happy ghasts.
 */
public class EntityClickHandler {
    private static final Logger logger = LoggerFactory.getLogger("happy-artillery");

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            Entity vehicle = player.getVehicle();
            if (vehicle == null || !vehicle.equals(entity)) return InteractionResult.PASS;
            if (!isHappyGhast(vehicle)) return InteractionResult.PASS;

            return handleStickAction(player, hand, vehicle, (ServerLevel) world);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            Entity vehicle = player.getVehicle();
            if (vehicle == null) return InteractionResult.PASS;
            if (!isHappyGhast(vehicle)) return InteractionResult.PASS;

            return handleStickAction(player, hand, vehicle, (ServerLevel) world);
        });
    }

    private static InteractionResult handleStickAction(net.minecraft.world.entity.player.Player player, InteractionHand hand, Entity vehicle, ServerLevel world) {
        ItemStack stack = player.getItemInHand(hand);
        if (isFireStick(stack)) {
            return shoot(player, vehicle, world);
        } else if (isCryStick(stack)) {
            return cry(player, vehicle);
        }
        return InteractionResult.PASS;
    }

    private static boolean isFireStick(ItemStack stack) {
        // Check for fire control tag (the new way)
        if (CustomDataComponents.hasFireControlTag(stack)) {
            return true;
        }
        // Fallback to original fire charge item check (if not using control system)
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(HAConstants.FIRE_STICK_ITEM);
    }

    private static boolean isCryStick(ItemStack stack) {
        // Check for cry control tag (the new way)
        if (CustomDataComponents.hasCryControlTag(stack)) {
            return true;
        }
        // Fallback to original ghast tear item check (if not using control system)
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(HAConstants.CRY_STICK_ITEM);
    }

    private static boolean isHappyGhast(Entity vehicle) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).toString();
        return id.equals(HAConstants.HAPPY_GHAST_ENTITY_ID);
    }

    private static InteractionResult cry(net.minecraft.world.entity.player.Player player, Entity vehicle) {
        if (vehicle.isInWater()) {
            return InteractionResult.FAIL;
        }
        if (!CooldownTracker.canCry(player.getUUID())) {
            return InteractionResult.FAIL;
        }
        CooldownTracker.recordCry(player.getUUID());
        ServerLevel world = (ServerLevel) ((EntityWorldAccessor) vehicle).happy$getLevel();
        world.playSound(null, vehicle.blockPosition(), SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, HAConstants.CRY_VOLUME(), 0.8f);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult shoot(net.minecraft.world.entity.player.Player player, Entity vehicle, ServerLevel world) {
        var ghastId = vehicle.getUUID();

        // Water prevents firing but cools
        if (vehicle.isInWater()) {
            CooldownTracker.applyWaterCooling(ghastId);
            return InteractionResult.FAIL;
        }

        // Heat / Overheat check
        int overheatLimit = biomeOverheatLimit(world, vehicle);
        double currentHeat = CooldownTracker.getFireballHeat(ghastId);
        if (currentHeat >= overheatLimit) {
            return InteractionResult.FAIL;
        }

        // Shoot cooldown
        if (!CooldownTracker.canShoot(ghastId)) {
            logger.debug("[HappyArtillery] Shoot blocked by cooldown");
            return InteractionResult.FAIL;
        }

        // Ammo check
        int cost = HAConstants.FIREBALL_AMMO_COST();
        int ammo = CooldownTracker.getAmmo(ghastId);
        if (ammo < cost) {
            return InteractionResult.FAIL;
        }
        CooldownTracker.useAmmo(ghastId, cost);

        // Apply heat (and detect explosion threshold)
        boolean willExplode = currentHeat + 1 >= overheatLimit;
        CooldownTracker.addFireballHeat(ghastId);
        CooldownTracker.recordShot(ghastId);

        if (willExplode) {
            performOverheatExplosion(world, vehicle, player);
            return InteractionResult.SUCCESS;
        }

        spawnFireball(world, vehicle, player);
        return InteractionResult.SUCCESS;
    }

    private static void spawnFireball(ServerLevel world, Entity mount, net.minecraft.world.entity.player.Player player) {
        Vec3 dir = player.getViewVector(1.0F).normalize();
        float eyeHeight = (mount instanceof LivingEntity) ? ((LivingEntity) mount).getEyeHeight() : (mount.getBbHeight() * 0.6f);
        double spawnX = mount.getX() + dir.x * 2.0;
        double spawnY = mount.getY() + eyeHeight;
        double spawnZ = mount.getZ() + dir.z * 2.0;
        double speed = 0.5;
        Vec3 velocity = new Vec3(dir.x * speed, dir.y * speed, dir.z * speed);
        LivingEntity owner = (mount instanceof LivingEntity) ? (LivingEntity) mount : player;
        
        LargeFireball fireball = new LargeFireball(world, owner, velocity, HAConstants.FIREBALL_EXPLOSION_POWER());
        fireball.setPos(spawnX, spawnY, spawnZ);

        boolean spawned = world.addFreshEntity(fireball);
        world.playSound(null, spawnX, spawnY, spawnZ, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.0f, 1.0f);
        logger.debug("[HappyArtillery] Spawned ghast fireball success={}", spawned);
        
        if (spawned) {
            // Force-load chunks along the fireball's path to ensure it can always hit something
            forceLoadFireballPath(world, spawnX, spawnY, spawnZ, velocity);
        } else {
            fallbackInstantRay(world, mount, dir, spawnX, spawnY, spawnZ);
        }
    }

    private static void forceLoadFireballPath(ServerLevel world, double startX, double startY, double startZ, Vec3 velocity) {
        // Pre-load chunks in the direction the fireball is traveling
        // This ensures chunks don't unload as the fireball flies through them
        Vec3 normalizedDir = velocity.normalize();
        double maxDistance = 128.0; // Load chunks up to 128 blocks away
        int steps = 16; // Check every 8 blocks
        
        for (int i = 0; i < steps; i++) {
            double distance = (i / (double) steps) * maxDistance;
            double checkX = startX + normalizedDir.x * distance;
            double checkY = startY + normalizedDir.y * distance;
            double checkZ = startZ + normalizedDir.z * distance;
            
            BlockPos pos = BlockPos.containing(checkX, checkY, checkZ);
            ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            
            // Force load the chunk - add ticket to keep chunks loaded
            world.getChunk(chunkPos.x(), chunkPos.z());
        }
    }

    private static void fallbackInstantRay(ServerLevel world, Entity mount, Vec3 dir, double sx, double sy, double sz) {
        Vec3 origin = new Vec3(sx, sy, sz);
        double maxDistance = 48.0;
        Vec3 target = origin.add(dir.scale(maxDistance));
        ClipContext ctx = new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mount);
        var hit = world.clip(ctx);
        Vec3 impact = (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) ? hit.getLocation() : target;
        int steps = (int) (maxDistance / 0.6);
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 point = origin.add(dir.scale(maxDistance * t));
            if (point.distanceToSqr(impact) < 0.4) break;
            world.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 2, 0.02, 0.02, 0.02, 0.001);
        }
        world.explode(null, impact.x, impact.y, impact.z, HAConstants.FIREBALL_EXPLOSION_POWER(), true, net.minecraft.world.level.Level.ExplosionInteraction.MOB);
        world.playSound(null, origin.x, origin.y, origin.z, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.0f, 1.0f);
        logger.debug("[HappyArtillery] Fallback ray fireball origin={} impact={}", origin, impact);
    }

    private static void performOverheatExplosion(ServerLevel world, Entity mount, net.minecraft.world.entity.player.Player player) {
        Vec3 dir = player.getViewVector(1.0F).normalize();
        float eyeHeight = (mount instanceof LivingEntity) ? ((LivingEntity) mount).getEyeHeight() : (mount.getBbHeight() * 0.6f);
        Vec3 explosionCenter = new Vec3(mount.getX() + dir.x * 2.0, mount.getY() + eyeHeight, mount.getZ() + dir.z * 2.0);
        
        // Create main explosion
        world.explode(null, explosionCenter.x, explosionCenter.y, explosionCenter.z, HAConstants.OVERHEAT_EXPLOSION_POWER(), HAConstants.OVERHEAT_EXPLOSION_CREATES_FIRE(), net.minecraft.world.level.Level.ExplosionInteraction.TNT);
        
        // Spawn fireballs in all directions (sphere pattern)
        spawnExplosionFireballSphere(world, mount, explosionCenter);
        
        // Spawn fire ring
        spawnFireRing(world, explosionCenter);
        
        logger.info("{}'s ghast overheated and exploded", player.getName().getString());
    }

    private static void spawnExplosionFireballSphere(ServerLevel world, Entity mount, Vec3 center) {
        // Create a dense sphere of fireballs around the explosion center
        LivingEntity owner = (mount instanceof LivingEntity) ? (LivingEntity) mount : null;
        int fireballs = 48; // More fireballs for denser sphere coverage
        
        for (int i = 0; i < fireballs; i++) {
            // Generate uniform sphere distribution using golden spiral
            double phi = Math.acos(2.0 * (i / (double) fireballs) - 1.0);
            double theta = Math.PI * (1.0 + Math.sqrt(5.0)) * i; // Golden angle
            
            double radius = 2.5;
            double vx = radius * Math.sin(phi) * Math.cos(theta);
            double vy = radius * Math.sin(phi) * Math.sin(theta);
            double vz = radius * Math.cos(phi);
            
            Vec3 velocity = new Vec3(vx, vy, vz);
            LargeFireball fireball = new LargeFireball(world, owner, velocity, HAConstants.FIREBALL_EXPLOSION_POWER());
            fireball.setPos(center.x, center.y, center.z);
            world.addFreshEntity(fireball);
        }
        
        world.playSound(null, center.x, center.y, center.z, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 2.0f, 0.8f);
    }

    private static void spawnFireRing(ServerLevel world, Vec3 center) {
        int radius = 5;
        for (int i = 0; i < 15; i++) {
            double angle = world.getRandom().nextDouble() * Math.PI * 2;
            double dist = world.getRandom().nextDouble() * radius;
            double fx = center.x + dist * Math.cos(angle);
            double fz = center.z + dist * Math.sin(angle);
            double fy = center.y + (world.getRandom().nextDouble() - 0.5) * 2;
            BlockPos bp = BlockPos.containing(fx, fy, fz);
            if (world.getBlockState(bp).isAir() && world.getBlockState(bp.below()).isSolid()) {
                world.setBlock(bp, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    private static int biomeOverheatLimit(ServerLevel world, Entity vehicle) {
        BlockPos pos = BlockPos.containing(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        var biome = world.getBiome(pos).value();
        float temp = biome.getBaseTemperature();
        
        if (world.dimension().identifier().toString().contains("nether") || temp >= 1.5f) {
            return HAConstants.HOT_BIOME_LIMIT();
        } else if (world.dimension().identifier().toString().contains("end") || temp <= 0.0f) {
            return HAConstants.COLD_BIOME_LIMIT();
        }
        return HAConstants.BASE_OVERHEAT_LIMIT();
    }
}
