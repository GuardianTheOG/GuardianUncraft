package com.guardianuncraft.util;

import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static ItemStack cloneStack(ItemStack item) {
        if (item == null) return null;
        return item.clone();
    }

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
