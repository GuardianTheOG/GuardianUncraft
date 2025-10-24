package com.guardianuncraft.gui;

import com.guardianuncraft.GuardianUncraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiManager {

    private final GuardianUncraft plugin;
    private final MiniMessage mm;
    private final PlainTextComponentSerializer plain;
    private final RecipeHandler recipeHandler;

    // GUI slot layout constants
    public static final int SLOT_CANCEL = 0;
    public static final int SLOT_ACCEPT = 8;
    public static final int SLOT_INPUT = 11;
    public static final int[] RECIPE_SLOTS = {4, 5, 6, 13, 14, 15, 22, 23, 24};

    public GuiManager(GuardianUncraft plugin) {
        this.plugin = plugin;
        this.mm = plugin.miniMessage();
        this.plain = PlainTextComponentSerializer.plainText();
        this.recipeHandler = new RecipeHandler(plugin);
    }

    public RecipeHandler getRecipeHandler() {
        return recipeHandler;
    }

    /**
     * Opens the Uncrafting GUI for the specified player.
     */
    public void openUncraftGui(Player player) {
        String rawTitle = plugin.getConfigManager().getConfig().getString("gui-title", "<gold><bold>Uncrafting Table");
        Component comp = mm.deserialize(rawTitle);
        String plainTitle = plain.serialize(comp);

        Inventory inv = Bukkit.createInventory(new UncraftInventoryHolder(null), 27, plainTitle);

        // Fill all slots with black stained glass
        ItemStack filler = createPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Cancel button
        inv.setItem(SLOT_CANCEL, createPane(Material.RED_STAINED_GLASS_PANE, mm.deserialize("<red><bold>Cancel")));

        // Accept button
        inv.setItem(SLOT_ACCEPT, createPane(Material.LIME_STAINED_GLASS_PANE, mm.deserialize("<green><bold>Accept")));

        // Input slot (for item to uncraft)
        inv.setItem(SLOT_INPUT, new ItemStack(Material.AIR));

        // Clear recipe grid
        clearRecipeSlots(inv);

        player.openInventory(inv);
    }

    /**
     * Populates the recipe grid based on the given input item.
     * If the recipe cannot be found or item is invalid, clears the grid.
     */
    public void populateRecipeGrid(Player player, Inventory inv, ItemStack input) {
        clearRecipeSlots(inv);

        if (input == null || input.getType() == Material.AIR) {
            return;
        }

        try {
            List<ItemStack> results = recipeHandler.getUncraftRecipe(player, input);

            if (results == null || results.isEmpty()) {
                return;
            }

            for (int i = 0; i < RECIPE_SLOTS.length; i++) {
                int slot = RECIPE_SLOTS[i];
                ItemStack toPlace = (i < results.size()) ? results.get(i) : null;
                inv.setItem(slot, (toPlace == null) ? new ItemStack(Material.AIR) : toPlace);
            }

        } catch (Exception ex) {
            plugin.getLogger().warning("Error while populating recipe grid: " + ex.getMessage());
            clearRecipeSlots(inv);
        }
    }

    /**
     * Clears all recipe slots in the GUI.
     */
    private void clearRecipeSlots(Inventory inv) {
        for (int slot : RECIPE_SLOTS) {
            inv.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    /**
     * Creates an ItemStack with a plain text name.
     */
    private ItemStack createPane(Material mat, String namePlain) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(namePlain);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an ItemStack with a MiniMessage component name.
     */
    private ItemStack createPane(Material mat, Component component) {
        String plainName = plain.serialize(component);
        return createPane(mat, plainName);
    }
}
