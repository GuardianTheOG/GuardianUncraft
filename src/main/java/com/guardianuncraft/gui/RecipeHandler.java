package com.guardianuncraft.gui;

import com.guardianuncraft.GuardianUncraft;
import com.guardianuncraft.config.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RecipeHandler {
    private final GuardianUncraft plugin;
    private final ConfigManager configManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public RecipeHandler(GuardianUncraft plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public List<ItemStack> getUncraftRecipe(Player player, ItemStack input) {
        if (input == null || input.getType() == Material.AIR) return null;

        Material material = input.getType();
        String matName = material.name();

        // Block disallowed items
        matName = input.getType().name();
        if (configManager.getRecipesConfig().isConfigurationSection(matName)) {
            boolean allowed = configManager.isAllowed(matName);
            if (!allowed) {
                plugin.getGuiManager().openUncraftGui(player);
                player.sendMessage(plugin.miniMessage().deserialize(plugin.getPrefix() + "<red>❌ Uncrafting of this item is not allowed!"));
                return null;
            }
        }

        // Block custom-named items
        ItemMeta meta = input.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            plugin.getGuiManager().openUncraftGui(player);
            player.sendMessage(mm.deserialize(plugin.getPrefix() + "<red>❌ Custom named items cannot be uncrafted."));
            return null;
        }

        // Try custom recipe first
        List<String> customIngredients = configManager.getCustomIngredients(matName);
        if (customIngredients != null) {
            List<ItemStack> result = new ArrayList<>();
            for (String ingr : customIngredients) {
                if (ingr == null || ingr.equalsIgnoreCase("AIR") || ingr.isEmpty()) continue;
                Material ingrMat = Material.matchMaterial(ingr);
                if (ingrMat != null && ingrMat != Material.AIR) {
                    result.add(new ItemStack(ingrMat, 1));
                }
            }

            // ✅ FIX: scale output by input count
            if (!result.isEmpty()) {
                for (ItemStack stack : result) {
                    stack.setAmount(stack.getAmount() * input.getAmount());
                }
            }

            return result.isEmpty() ? null : result;
        }

        // Try Bukkit recipes
        return findVanillaRecipe(player, input);
    }

    private List<ItemStack> findVanillaRecipe(Player player, ItemStack input) {
        List<ItemStack> result = new ArrayList<>();
        List<Recipe> recipes = Bukkit.getRecipesFor(input);

        if (recipes == null || recipes.isEmpty()) {
            player.sendMessage(mm.deserialize(plugin.getPrefix() + " <red>❌ No recipe found for this item."));
            return null;
        }

        Recipe recipe = recipes.get(0);
        int resultCount = recipe.getResult().getAmount(); // how many items are crafted normally
        int inputCount = input.getAmount();

        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> choiceMap = shaped.getChoiceMap();
            for (RecipeChoice choice : choiceMap.values()) {
                if (choice != null) {
                    ItemStack item = choiceToItem(choice);
                    if (item != null && item.getType() != Material.AIR) {
                        ItemStack clone = item.clone();
                        // ✅ FIX: scale ingredient amount by ratio
                        clone.setAmount(clone.getAmount() * (inputCount / resultCount));
                        result.add(clone);
                    }
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            List<RecipeChoice> choices = shapeless.getChoiceList();
            for (RecipeChoice choice : choices) {
                ItemStack item = choiceToItem(choice);
                if (item != null && item.getType() != Material.AIR) {
                    ItemStack clone = item.clone();
                    clone.setAmount(clone.getAmount() * (inputCount / resultCount));
                    result.add(clone);
                }
            }
        } else if (recipe instanceof SmithingRecipe smithing) {
            RecipeChoice base = smithing.getBase();
            RecipeChoice addition = smithing.getAddition();
            if (base != null) {
                ItemStack baseItem = choiceToItem(base);
                baseItem.setAmount(baseItem.getAmount() * (inputCount / resultCount));
                result.add(baseItem);
            }
            if (addition != null) {
                ItemStack addItem = choiceToItem(addition);
                addItem.setAmount(addItem.getAmount() * (inputCount / resultCount));
                result.add(addItem);
            }
        }

        // Enforce exact multiples (still valid)
        if (resultCount > 1 && inputCount % resultCount != 0) {
            player.sendMessage(mm.deserialize(plugin.getPrefix() + " <yellow>⚠ You need to uncraft a multiple of " + resultCount + " items."));
            return null;
        }

        return result.isEmpty() ? null : result;
    }

    private ItemStack choiceToItem(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.MaterialChoice mc) {
            return new ItemStack(mc.getChoices().get(0));
        }
        if (choice instanceof RecipeChoice.ExactChoice ec) {
            return ec.getChoices().get(0).clone();
        }
        return new ItemStack(Material.AIR);
    }
}
