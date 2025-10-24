package com.guardianuncraft.listeners;

import com.guardianuncraft.GuardianUncraft;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class RightClickListener implements Listener {

    private final GuardianUncraft plugin;

    public RightClickListener(GuardianUncraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // only right-click block
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        // only main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CRAFTING_TABLE) return;

        if (!plugin.getConfigManager().getConfig().getBoolean("enable-rightclick", true)) return;

        Player player = event.getPlayer();

        // only if sneaking and main hand empty
        if (player.isSneaking() &&
                (player.getInventory().getItemInMainHand() == null ||
                        player.getInventory().getItemInMainHand().getType() == Material.AIR)) {

            // cancel the vanilla open and open our GUI
            event.setCancelled(true);
            plugin.getGuiManager().openUncraftGui(player);
        } else {
            // allow vanilla behaviour (no action)
        }
    }
}
