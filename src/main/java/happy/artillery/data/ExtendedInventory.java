package happy.artillery.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages per-player extended inventory state (stored hotbar).
 * Simplified version without NBT persistence for 1.21.11 compatibility.
 */
public class ExtendedInventory {
    private List<ItemStack> storedHotbar = null;

    public boolean hasStoredHotbar() {
        return storedHotbar != null && !storedHotbar.isEmpty();
    }

    public void restoreHotbar(net.minecraft.world.entity.player.Inventory inventory) {
        if (storedHotbar == null) return;
        for (int i = 0; i < 9 && i < storedHotbar.size(); i++) {
            inventory.setItem(i, storedHotbar.get(i).copy());
        }
        storedHotbar = null;
    }

    public CompoundTag toNbt(HolderLookup.Provider lookup) {
        // Simplified: just return empty NBT for now
        // Full ItemStack CODEC serialization requires more complex DynamicOps setup
        return new CompoundTag();
    }

    public void fromNbt(CompoundTag tag, HolderLookup.Provider lookup) {
        // Simplified: no-op for now
        // In-memory storage is sufficient; hotbar resets when player quits
    }
}
