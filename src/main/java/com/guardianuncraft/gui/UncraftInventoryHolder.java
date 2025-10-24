package com.guardianuncraft.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class UncraftInventoryHolder implements InventoryHolder {

    private final Inventory inventory;

    public UncraftInventoryHolder(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
