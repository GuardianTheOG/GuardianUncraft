package com.guardianuncraft.commands;

import com.guardianuncraft.GuardianUncraft;
import com.guardianuncraft.config.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UncraftCommand implements CommandExecutor {
    private final GuardianUncraft plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public UncraftCommand(GuardianUncraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("guardianuncraft.admin")) {
                sender.sendMessage(mm.deserialize(plugin.getPrefix() + "<red>You do not have permission to use this command."));
                return true;
            }
            plugin.getConfigManager().reload();
            sender.sendMessage(mm.deserialize(plugin.getPrefix() + "✅ <green>Guardian-Uncraft reloaded successfully!"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>This command can only be run by a player."));
            return true;
        }

        plugin.getGuiManager().openUncraftGui(player);
        return true;
    }
}
