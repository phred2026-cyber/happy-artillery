package happy.artillery.mixin;

import happy.artillery.ControlSlotTagSyncer;
import happy.artillery.config.HAConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {
    
    @Inject(method = "clicked", at = @At("HEAD"))
    private void ha_handleControlSlotChanges(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer)) return;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        
        if (serverPlayer.getVehicle() == null) return;
        
        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(serverPlayer.getVehicle().getType()).toString();
        if (!typeId.equals(HAConstants.HAPPY_GHAST_ENTITY_ID)) return;

        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
        
        var slot = handler.getSlot(slotIndex);
        var inv = slot.container;
        
        // Only if the slot belongs to the player's own inventory
        if (inv == serverPlayer.getInventory()) {
            int slotIdx = slot.index;
            
            // Handle control slots (4 and 5) - schedule tag sync when items move
            if (slotIdx == 4 || slotIdx == 5) {
                ControlSlotTagSyncer.scheduleTagSync(serverPlayer, slotIdx);
            }
        }
    }
}
