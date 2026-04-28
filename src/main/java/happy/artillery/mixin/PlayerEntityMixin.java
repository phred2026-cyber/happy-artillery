package happy.artillery.mixin;

import happy.artillery.data.ExtendedInventory;
import happy.artillery.HappyArtilleryPlayerExtension;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements HappyArtilleryPlayerExtension {
    @Unique
    private final ExtendedInventory happy_artillery$extendedInventory = new ExtendedInventory();
    
    @Override
    public ExtendedInventory getExtendedInventory() {
        return happy_artillery$extendedInventory;
    }
}
