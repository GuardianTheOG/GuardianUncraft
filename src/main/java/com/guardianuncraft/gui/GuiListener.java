package com.guardianuncraft.gui;

import com.guardianuncraft.GuardianUncraft;
import com.guardianuncraft.util.ItemUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GuiListener implements Listener {

    private final GuardianUncraft plugin;
    private final MiniMessage mm;
    private final PlainTextComponentSerializer plain;

    public GuiListener(GuardianUncraft plugin) {
        this.plugin = plugin;
        this.mm = plugin.miniMessage();
        this.plain = PlainTextComponentSerializer.plainText();
    }

    private boolean isOurGui(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof UncraftInventoryHolder;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!isOurGui(inv)) return;

        Player player = (Player) event.getWhoClicked();
        int raw = event.getRawSlot();

        // Prevent all shift-clicks in the GUI
        if (event.isShiftClick()) {
            event.setCancelled(true);
            player.sendMessage(mm.deserialize(plugin.getPrefix() + " <yellow>Shift-clicking is disabled in this GUI."));
            return;
        }

        // Accept button
        if (raw == GuiManager.SLOT_ACCEPT) {
            event.setCancelled(true);
            handleAccept(player, inv);
            return;
        }

        // Cancel button
        if (raw == GuiManager.SLOT_CANCEL) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(mm.deserialize(plugin.getConfigManager().getConfig().getString("prefix") + "<yellow>Uncraft canceled."));
            return;
        }

        // Input slot
        if (raw == GuiManager.SLOT_INPUT) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    plugin.getGuiManager().populateRecipeGrid(player, inv, inv.getItem(GuiManager.SLOT_INPUT)), 1L);
            return;
        }

        // Recipe slots - cannot edit
        for (int s : GuiManager.RECIPE_SLOTS) {
            if (raw == s) {
                event.setCancelled(true);
                return;
            }
        }

        // All other GUI slots (filler)
        if (raw < inv.getSize()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        if (!isOurGui(inv)) return;

        // Prevent placing into anything except input slot
        for (int slot : event.getRawSlots()) {
            if (slot < inv.getSize() && slot != GuiManager.SLOT_INPUT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!isOurGui(inv)) return;

        Player player = (Player) event.getPlayer();
        ItemStack input = inv.getItem(GuiManager.SLOT_INPUT);
        if (!ItemUtils.isEmpty(input)) {
            player.getInventory().addItem(input);
            inv.setItem(GuiManager.SLOT_INPUT, null);
        }
    }

    private void handleAccept(Player player, Inventory inv) {
        ItemStack input = inv.getItem(GuiManager.SLOT_INPUT);
        if (ItemUtils.isEmpty(input)) {
            player.sendMessage(mm.deserialize(plugin.getConfigManager().getConfig().getString("prefix") + "<red>No item to uncraft."));
            return;
        }

        // Give items from recipe grid
        for (int slot : GuiManager.RECIPE_SLOTS) {
            ItemStack out = inv.getItem(slot);
            if (!ItemUtils.isEmpty(out)) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(out);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        inv.setItem(GuiManager.SLOT_INPUT, null);
        player.closeInventory();
        player.sendMessage(mm.deserialize(plugin.getConfigManager().getConfig().getString("prefix") + "<green>Uncrafted successfully!"));
    }
}
