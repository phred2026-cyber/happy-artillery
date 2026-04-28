package happy.artillery.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityWorldAccessor {
    // Yarn field for Entity's world
    @Accessor("world")
    Level happy$getLevel();
}
