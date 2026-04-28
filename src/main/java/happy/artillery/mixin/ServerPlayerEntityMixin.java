package happy.artillery.mixin;

import happy.artillery.HappyArtilleryPlayerExtension;
import happy.artillery.data.ExtendedInventory;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to inject the HappyArtilleryPlayerExtension interface into ServerPlayer.
 * This allows us to store extended inventory data on players.
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin implements HappyArtilleryPlayerExtension {
    private final ExtendedInventory extendedInventory = new ExtendedInventory();

    @Override
    public ExtendedInventory getExtendedInventory() {
        return extendedInventory;
    }
}
