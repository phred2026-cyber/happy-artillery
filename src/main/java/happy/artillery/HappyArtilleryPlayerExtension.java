package happy.artillery;

import happy.artillery.data.ExtendedInventory;

/**
 * Interface injected into ServerPlayer via mixin.
 */
public interface HappyArtilleryPlayerExtension {
    ExtendedInventory getExtendedInventory();
}
