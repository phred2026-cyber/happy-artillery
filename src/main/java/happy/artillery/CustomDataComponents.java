package happy.artillery;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple marker-based control for items - works server-side only, no client mods needed.
 * Uses hidden lore lines stored in item data components.
 */
public class CustomDataComponents {
    public static final String FIRE_CONTROL_TAG = "FireControl";
    public static final String CRY_CONTROL_TAG = "CryControl";
    public static final String TEMPORARY_TAG = "Temporary";

    public static void setFireControlTag(ItemStack stack) {
        setLoreTag(stack, FIRE_CONTROL_TAG);
    }

    public static void setCryControlTag(ItemStack stack) {
        setLoreTag(stack, CRY_CONTROL_TAG);
    }

    public static void setTemporaryTag(ItemStack stack) {
        setLoreTag(stack, TEMPORARY_TAG);
    }

    public static boolean hasFireControlTag(ItemStack stack) {
        return hasLoreTag(stack, FIRE_CONTROL_TAG);
    }

    public static boolean hasCryControlTag(ItemStack stack) {
        return hasLoreTag(stack, CRY_CONTROL_TAG);
    }

    public static boolean hasTemporaryTag(ItemStack stack) {
        return hasLoreTag(stack, TEMPORARY_TAG);
    }

    public static void removeFireControlTag(ItemStack stack) {
        removeLoreTag(stack, FIRE_CONTROL_TAG);
    }

    public static void removeCryControlTag(ItemStack stack) {
        removeLoreTag(stack, CRY_CONTROL_TAG);
    }

    public static void removeTemporaryTag(ItemStack stack) {
        removeLoreTag(stack, TEMPORARY_TAG);
    }

    public static void setFireControlName(ItemStack stack) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7c\ud83d\udd25 Fire Control"));
    }

    public static void setCryControlName(ItemStack stack) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a75\ud83d\udc7b Cry Control"));
    }

    public static void makeItemImmovable(ItemStack stack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static void initialize() {
        // Nothing to do - data components are applied directly to item stacks.
    }

    private static void setLoreTag(ItemStack stack, String tag) {
        List<Component> lines = getLoreLines(stack);
        boolean present = lines.stream().anyMatch(line -> line.getString().contains(tag));
        if (!present) {
            lines.add(Component.literal("§0§k" + tag));
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
    }

    private static boolean hasLoreTag(ItemStack stack, String tag) {
        if (stack.isEmpty()) {
            return false;
        }
        return getLoreLines(stack).stream().anyMatch(line -> line.getString().contains(tag));
    }

    private static void removeLoreTag(ItemStack stack, String tag) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return;
        }

        List<Component> lines = new ArrayList<>(lore.lines());
        lines.removeIf(line -> line.getString().contains(tag));
        if (lines.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
    }

    private static List<Component> getLoreLines(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        return lore == null ? new ArrayList<>() : new ArrayList<>(lore.lines());
    }
}