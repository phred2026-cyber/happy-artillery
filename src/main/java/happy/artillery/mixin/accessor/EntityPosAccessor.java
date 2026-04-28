package happy.artillery.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityPosAccessor {
    @Accessor("position")
    Vec3 happy$getPos();
}
